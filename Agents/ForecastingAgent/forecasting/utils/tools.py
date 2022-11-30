###############################################
# Authors: Magnus Mueller (mm2692@cam.ac.uk)  #
# Date: 11 Okt 2022                           #
###############################################


from forecasting.datamodel.iris import *

from darts import concatenate
import numpy as np
from darts import TimeSeries
from darts.utils.timeseries_generation import datetime_attribute_timeseries as dt_attr
from darts.dataprocessing.transformers import Scaler
import pandas as pd
from darts import concatenate
from forecasting.errorhandling.exceptions import KGException


def get_properties_for_subj(subj: str, verb_obj: dict = {}, verb_literal: dict = {}):
    """
         Constructs proper SPARQL update string from given name, verb-object and verb-literal dictionary.
         Arguments:
              subj: subject
              verb_obj:  object properties: dictionary of verb-object pairs, if value is a list, then it will be treated as multiple objects for the same verb
              verb_literal: data properties: dictionary of verb-literal pairs, if value is a list, then it will be treated as a literal with type
         Returns:
              SPARQL update string
    """
    output = f''''''
    #  object properties
    for verb, obj in verb_obj.items():
        # turn obj to list if it is not to iterate over it
        if not isinstance(obj, list):
            obj = [obj]
        for o in obj:
            output += f'''{create_triple(subj, verb, o)}'''

    # data properties
    for verb, literal in verb_literal.items():
        if not isinstance(literal, list):
            literal = [literal]
        if len(literal) == 1:
            literal.append(None)
        # check if len is max 2 for [literal, type]
        if len(literal) > 2:
            raise ValueError(
                f'Literal {literal} has more than two elements expected [literal, type] or [literal]')
        if not isinstance(literal[0], str):
            literal[0] = str(literal[0])
        # check that literal does not start with < and end with >
        if literal[0].startswith('<') and literal[0].endswith('>'):
            raise ValueError(f'Literal {literal} starts with < or ends with >')
        output += f'''{create_triple_literal(subj, verb, literal[0], literal[1])}'''

    return output


def get_predecessor_type_and_predicate(iri, kgClient):
    # get predecessor type of iri
    query = f"""
    SELECT ?predecessorType ?predicate
    WHERE {{
        ?o ?predicate <{iri}> . 
        ?o <{RDF_TYPE}> ?predecessorType . 
        }}"""
    cfg = kgClient.performQuery(query)
    return {r["predicate"]: r["predecessorType"] for r in cfg}


def get_ts_value_iri(iri, kgClient):
    # get ts value iri of iri
    query = f"""
    SELECT ?tsValueIRI
    WHERE {{
        <{iri}> <{OM_HASVALUE}> ?tsValueIRI . 
        }}"""
    ts_value_iri = kgClient.performQuery(query)[0]["tsValueIRI"]
    return ts_value_iri


def check_cov_matches_rdf_type(row, rdf_type):
    if 'type_with_measure' in row and row['type_with_measure'] == rdf_type or 'type_without_measure' in row and row['type_without_measure'] == rdf_type:
        return True
    else:
        return False


def get_covs_heat_supply(kgClient, tsClient,  lowerbound, upperbound, df=None):
    cov_iris = []

    # find covariates by specific type of ts iri.
    # This just works for the data iri that is used and has unique type.
    # e.g. in district heating it is not possible to find the covariates for the ts type of EnergyInTimeInterval, because its not unique
    # the covariate must be found through different identification
    ts_by_type = kgClient.performQuery(get_ts_by_type())
    # two different types of ts:
    # 1. ts with MEASURE
    # 2. ts without MEASURE
    for row in ts_by_type:
        if check_cov_matches_rdf_type(row, ONTOEMS_AIRTEMPERATURE):
            df_air_temp = get_df_of_ts(
                row['tsIRI'], tsClient, lowerbound=lowerbound, upperbound=upperbound)
            cov_iris.append(row['tsIRI'])

        if check_cov_matches_rdf_type(row, OHN_ISPUBLICHOLIDAY):
            df_public_holiday = get_df_of_ts(
                row['tsIRI'], tsClient, lowerbound=lowerbound, upperbound=upperbound)
            cov_iris.append(row['tsIRI'])

    # create covariates list with time covariates for the forecast
    # Attention: be aware to have same order of covariates as during training
    covariates = concatenate(
        [
            get_data_cov(df_air_temp, "cov"),
            # use dates of other covariate to extract time covariates
            # if no other covariate is available, use df of orginal series
            # TODO:
            # in that case you need to extend the time range of the original series
            # in order to have enough data for the future time covariates (length of forecast horizon)
            get_time_cov(
                df_air_temp, {"dayofyear": True, "dayofweek": True, "hour": True}),
            get_data_cov(df_public_holiday, "cov"),
        ],
        axis="component",
    )

    # add darts covariates as string
    cov_iris += ['dayofyear', 'dayofweek', 'hour']
    return cov_iris, covariates


def get_df_of_ts(tsIRI, tsClient, lowerbound, upperbound, column_name="cov", date_name="Date"):
    dates, values = get_ts_data(
        tsIRI, tsClient,  lowerbound=lowerbound, upperbound=upperbound)
    if len(values) == 0:
        raise ValueError(
            f"no values for tsIRI {tsIRI}, with lowerbound {lowerbound} and upperbound {upperbound}")
    df = pd.DataFrame(zip(values, dates), columns=[column_name, date_name])
    # remove time zone
    df.Date = pd.to_datetime(df.Date).dt.tz_convert('UTC').dt.tz_localize(None)
    return df


def get_unit(iri, kgClient):
    # get unit of iri
    query = f"""
    SELECT ?unit
    WHERE {{
        <{iri}> <{OM_HASVALUE}> ?measure . 
        ?measure <{OM_HASUNIT}> ?unit . 
        }}"""
    unit = kgClient.performQuery(query)[0]["unit"]
    return unit


def get_time_format(iri, kgClient):
    # get unit of iri
    query = f"""
    SELECT ?format
    WHERE {{
        <{iri}> <{OM_HASVALUE}> ?measure . 
        ?measure <{TS_HASTIMESERIES}> ?ts . 
        ?ts <{TS_HASTIMEUNIT}> ?format . 
        }}"""
    time_format = kgClient.performQuery(query)[0]["format"]
    return time_format


def get_ts_data(iri, ts_client, lowerbound=None, upperbound=None):
    """
    It takes a data IRI and a client object, and returns the dates and values of the time series

    :param iri: The IRI of the data you want to get
    :param ts_client: a TimeSeriesClient object
    :return: A tuple of two lists. The first list is a list of dates, the second list is a list of
    values.
    """

    if lowerbound is not None and upperbound is not None:
        try:
            with ts_client.connect() as conn:
                ts = ts_client.tsclient.getTimeSeriesWithinBounds(
                    [iri], lowerbound, upperbound, conn)
        except:
            raise KGException(
                f"Could not get time series for {iri} with lowerbound {lowerbound} and upperbound {upperbound}")
    else:
        try:
            with ts_client.connect() as conn:
                ts = ts_client.tsclient.getTimeSeries([iri], conn)
        except:
            raise KGException(
                f"Could not get time series for {iri} with lowerbound {lowerbound} and upperbound {upperbound}")

    dates = ts.getTimes()
    # Unwrap Java time objects
    dates = [d.toString() for d in dates]
    values = ts.getValues(iri)
    return dates, values


def get_ts_by_type():
    """
    It returns a SPARQL query that will return all the iris and tsIRIs in the graph, along with the
    type of the iri.

    It distinguishes between measures and non-measures. 
    The type of Timeseries with measures is returned under the key 'type_with_measure' and those without measures are returned under the key 'type_without_measure'.
    """

    return f"""
     prefix rdf: <{RDF}>
     prefix om: <{OM}>
     prefix ts: <{TS}>
     SELECT  distinct ?iri ?tsIRI ?type_with_measure ?type_without_measure
     WHERE {{
      
       ?tsIRI ts:hasTimeSeries ?ts . 
       ?tsIRI rdf:type ?type_without_measure .
       OPTIONAL {{ ?iri om:hasValue ?tsIRI . ?iri rdf:type ?type_with_measure }} . 
       
     }}
     """


def get_data_cov(df, col):
    """
    It takes a dataframe and a column name as input, and returns a scaled time series

    :param df: the dataframe containing the data
    :param col: the column name of the dataframe that you want to use
    :return: A time series object
    """

    cov = TimeSeries.from_dataframe(df, time_col='Date', value_cols=col)
    scaler_cov = Scaler()
    cov_scaled = scaler_cov.fit_transform(cov)
    return cov_scaled


def get_time_cov(df, cov: dict):
    """
    It takes a df and returns a covariate matrix with the following columns:

    - day of year (cyclic)
    - day of week (cyclic)
    - hour of day (cyclic)

    The `cyclic` keyword argument is used to indicate that the covariate is cyclic. This is important
    for the model to learn the correct periodicity

    :param series: the time series to be modeled
    :return: A covariate matrix with the following columns:
        - day of year (cyclic)
        - day of week (cyclic)
        - hour of day (cyclic)
    """

    series = TimeSeries.from_dataframe(
        df, time_col='Date')  # , fill_missing_dates =True

    covs = concatenate(
        [
            dt_attr(series.time_index, k, dtype=np.float32, cyclic=v) for k, v in cov.items()

        ],
        axis="component",
    )
    return covs


def add_insert_data(update):
    """
    It takes a string of SPARQL update statements and returns a string of SPARQL update statements that
    will insert the data described by the original string

    :param update: the update query
    :return: the string "INSERT DATA\n{\n" + update + "}"
    """

    return "INSERT DATA\n{\n" + update + "}"


def create_sparql_prefix(iri):
    """
        Constructs proper SPARQL Prefix String from given IRI
        Arguments:
               iri: IRI of the prefix
        Returns:
            SPARQL update prefix string in the form "PREFIX ns: <full IRI>".
    """

    if not iri.startswith('<'):
        iri = '<' + iri
    if not iri.endswith('>'):
        iri = iri + '>'

    return 'PREFIX ' + iri.__name__ + ': ' + iri + ' '


def add_enclosing(s):
    """
         Adds enclosing < > to a string if it does not have it already.
         Arguments:
              s: string
         Returns:
              string with enclosing < >
    """
    if not isinstance(s, str):
        s = str(s)
    if not s.startswith('<'):
        s = '<' + s
    if not s.endswith('>'):
        s = s + '>'
    return s


def max_error(s1, s2):
    """
    It takes two time series, slices them to the intersection of their time ranges, and then returns the
    maximum absolute difference between the two time series

    :param s1: the first time series
    :param s2: the original data
    :return: The maximum error between the two time series.
    """
    s2 = s2.slice_intersect(s1)
    s1 = s1.slice_intersect(s2)
    return np.max(np.abs(s1.values() - s2.values()))


def create_triple(subject, predicate, obj):
    """
         Constructs object properties from given subject, predicate and object.
         Literals are handled by create_triple_literal
         Arguments:
              subject: subject of the triple
              predicate: predicate of the triple
              obj: object of the triple
         Returns:
              SPARQL triple string in the form "<subject> <predicate> <object>."
    """

    # typechecking
    # if varibles do not have enclosing <> add them
    subject = add_enclosing(subject)
    predicate = add_enclosing(predicate)
    obj = add_enclosing(obj)

    output = subject + ' ' + predicate + ' ' + obj + ' . \n'
    return output


def create_triple_literal(subject, predicate, literal, literal_type=None):
    """
         Constructs data properties triple string from given subject, predicate and literal.
         Arguments:
              subject: subject of the triple
              predicate: predicate of the triple
              literal: literal of the triple
              literal_type: type of the literal, e.g. XSD_STRING (optional)
         Returns:
              data properties string in the form "<subject> <predicate> 'literal' . "
    """
    # if literal is not a string, convert it
    if not isinstance(literal, str):
        literal = str(literal)
    if literal_type is None:
        # return without type
        output = '<' + subject + '> <' + predicate + '> "' + literal + '" . \n'
    else:
        # return with type
        output = '<' + subject + '> <' + predicate + '> "' + \
            literal + '"^^<' + literal_type + '> . \n'
    return output


def get_properties_for_subj(subj: str, verb_obj: dict = {}, verb_literal: dict = {}):
    """
         Constructs proper SPARQL update string from given name, verb-object and verb-literal dictionary.
         Arguments:
              subj: subject
              verb_obj:  object properties: dictionary of verb-object pairs, if value is a list, then it will be treated as multiple objects for the same verb
              verb_literal: data properties: dictionary of verb-literal pairs, if value is a list, then it will be treated as a literal with type
         Returns:
              SPARQL update string
    """
    output = f''''''
    #  object properties
    for verb, obj in verb_obj.items():
        # turn obj to list if it is not to iterate over it
        if not isinstance(obj, list):
            obj = [obj]
        for o in obj:
            output += f'''{create_triple(subj, verb, o)}'''

    # data properties
    for verb, literal in verb_literal.items():
        if not isinstance(literal, list):
            literal = [literal]
        if len(literal) == 1:
            literal.append(None)
        # check if len is max 2 for [literal, type]
        if len(literal) > 2:
            raise ValueError(
                f'Literal {literal} has more than two elements expected [literal, type] or [literal]')
        if not isinstance(literal[0], str):
            literal[0] = str(literal[0])
        # check that literal does not start with < and end with >
        if literal[0].startswith('<') and literal[0].endswith('>'):
            raise ValueError(f'Literal {literal} starts with < or ends with >')
        output += f'''{create_triple_literal(subj, verb, literal[0], literal[1])}'''

    return output
