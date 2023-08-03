#################################################
# Authors: Markus Hofmeister (mh807@cam.ac.uk)  #
#          Magnus Mueller (mm2692@cam.ac.uk)    #
# Date: 25 Jul 2023                             #
#################################################

# The purpose of this module is to test the Agent Flask App. HTTP requests are sent 
# to the Flask App and the response is checked.

import pytest
import json
import requests
from pathlib import Path
from rdflib import Graph
from rdflib import RDF
from operator import eq, gt

from py4jps import agentlogging

import forecastingagent.datamodel as dm
from forecastingagent.agent.forcasting_config import DOUBLE, TIME_FORMAT

from . import conftest as cf


# Initialise logger instance (ensure consistent logger level`)
logger = agentlogging.get_logger('prod')


def test_example_triples():
    """
    This test checks that the example triples are correct in syntax.

    Raises:
        e: If the example triples are not valid RDF.
    """
    g = Graph()
    pathlist = Path(cf.TEST_TRIPLES_DIR).glob('*.ttl')
    for path in pathlist:
        try:
            g.parse(str(path))
        except Exception as e:
            raise e


def test_example_data_instantiation(initialise_clients):
    """
    This test checks that all example data gets correctly instantiated,
    including associated time series data in PostgreSQL.
    """
    # Get required clients from fixture
    sparql_client, ts_client, _, rdb_url = initialise_clients

    ### TRIPPLE STORE ###
    # Verify that KG is empty
    assert sparql_client.getAmountOfTriples() == 0

    # Upload example test triples
    cf.initialise_triples(sparql_client)

    # Verify instantiation of expected number of triples
    triples = cf.TBOX_TRIPLES + cf.ABOX_TRIPLES
    assert sparql_client.getAmountOfTriples() == triples

    ### POSTGRESQL ###
    # Verify that Postgres database is empty
    assert cf.get_number_of_rdb_tables(rdb_url) == 0

    # Initialise and upload time series
    ts_client.init_timeseries(dataIRI=cf.IRI_TO_FORECAST_1,
                              times=cf.TIMES, values=cf.VALUES_1,
                              ts_type=DOUBLE, time_format=TIME_FORMAT)

    # Verify that expected tables and triples are created (i.e. dbTable + 1 ts table)
    assert cf.get_number_of_rdb_tables(rdb_url) == 2
    assert sparql_client.getAmountOfTriples() == (triples + cf.TS_TRIPLES)

    # Verify correct retrieval of time series data
    times, values = ts_client.retrieve_timeseries(cf.IRI_TO_FORECAST_1)
    assert times == cf.TIMES
    # Account for rounding errors
    assert pytest.approx(values, rel=1e-5) == cf.VALUES_1

    # Verify that dropping all tables works as expected
    cf.clear_database(rdb_url)
    assert cf.get_number_of_rdb_tables(rdb_url) == 0


@pytest.mark.parametrize(
    "derivation_input_set, iri_to_forecast, ts_times, ts_values",
    [
        (cf.DERIVATION_INPUTS_1, cf.IRI_TO_FORECAST_1, cf.TIMES, cf.VALUES_1),
        (cf.DERIVATION_INPUTS_2, cf.IRI_TO_FORECAST_2, cf.TIMES, cf.VALUES_3)
    ],
)
def test_create_forecast(
    initialise_clients, create_example_agent, derivation_input_set, iri_to_forecast,
    ts_times, ts_values
):
    """
    Test if forecasting agent performs derivation update as expected
    """

    # Get required clients from fixture
    sparql_client, ts_client, derivation_client, rdb_url = initialise_clients

    # Initialise all triples in test_triples + initialise time series in RDB
    # (it first DELETES ALL DATA in the specified SPARQL/RDB endpoints)
    cf.initialise_triples(sparql_client)
    cf.clear_database(rdb_url)
    ts_client.init_timeseries(dataIRI=iri_to_forecast,
                              times=ts_times, values=ts_values,
                              ts_type=DOUBLE, time_format=TIME_FORMAT)

    # Verify correct number of triples (not marked up with timestamp yet)
    triples = cf.TBOX_TRIPLES + cf.ABOX_TRIPLES + cf.TS_TRIPLES
    assert sparql_client.getAmountOfTriples() == triples

    # Create agent instance and register agent in KG
    # - Successful agent registration within the KG is required to pick up marked up derivations
    # - Hence, the Dockerised agent is started without initial registration within the Stack and
    #   registration is done within the test to guarantee that test Blazegraph will be ready
    # - The "belated" registration of the Dockerised agent can be achieved by registering "another"
    #   agent instance with the same ONTOAGENT_SERVICE_IRI
    agent = create_example_agent(random_agent_iri=True)

    # Verify expected number of triples after derivation registration
    triples += cf.AGENT_SERVICE_TRIPLES
    triples += cf.DERIV_INPUT_TRIPLES + cf.DERIV_OUTPUT_TRIPLES
    assert sparql_client.getAmountOfTriples() == triples

    # Assert that there's currently no instance having rdf:type of the output signature in the KG
    assert not sparql_client.check_if_triple_exist(None, RDF.type.toPython(), dm.TS_FORECAST)

    # Create derivation instance for new information (incl. timestamps for pure inputs)
    derivation = derivation_client.createSyncDerivationForNewInfo(agent.agentIRI, derivation_input_set,
                                                                  cf.ONTODERIVATION_DERIVATIONWITHTIMESERIES)
    # derivation_iri = derivation.getIri()
    # print(f"Initialised successfully, created synchronous derivation instance: {derivation_iri}")
    
    # # Verify expected number of triples after derivation registration
    # triples += cf.TIME_TRIPLES_PER_PURE_INPUT * len(derivation_input_set) # timestamps for pure inputs
    # triples += cf.TIME_TRIPLES_PER_PURE_INPUT                             # timestamps for derivation instance
    # triples += len(derivation_input_set) + 2    # number of inputs + derivation type + associated agent 
    # assert sparql_client.getAmountOfTriples() == triples

    # # Query timestamp of the derivation for every 20 seconds until it's updated
    # currentTimestamp_derivation = 0
    # while currentTimestamp_derivation == 0:
    #     time.sleep(10)
    #     currentTimestamp_derivation = cf.get_timestamp(derivation_iri, sparql_client)

    # # Query the output of the derivation instance
    # derivation_outputs = cf.get_derivation_outputs(derivation_iri, sparql_client)
    # print(f"Generated derivation outputs that belongsTo the derivation instance: {', '.join(derivation_outputs)}")
    
    # # Verify that there are 2 derivation outputs (i.e. AveragePrice and Measure IRIs)
    # assert len(derivation_outputs) == 2
    # assert dm.OBE_AVERAGE_SM_PRICE in derivation_outputs
    # assert len(derivation_outputs[dm.OBE_AVERAGE_SM_PRICE]) == 1
    # assert dm.OM_MEASURE in derivation_outputs
    # assert len(derivation_outputs[dm.OM_MEASURE]) == 1
    
    # # Verify the values of the derivation output
    # avg_iri = derivation_outputs[dm.OBE_AVERAGE_SM_PRICE][0]
    # inputs, postcode, price = cf.get_avgsqmprice_details(sparql_client, avg_iri)
    # # Verify postcode
    # assert len(postcode) == 1
    # assert postcode[0] == expected_postcode
    # # Verify price
    # assert len(price) == 1
    # assert price[0] == expected_avg

    # # Verify inputs (i.e. derived from)
    # # Create deeepcopy to avoid modifying original cf.DERIVATION_INPUTS_... between tests
    # derivation_input_set_copy = copy.deepcopy(derivation_input_set)
    # for i in inputs:
    #     for j in inputs[i]:
    #         assert j in derivation_input_set_copy
    #         derivation_input_set_copy.remove(j)
    # assert len(derivation_input_set_copy) == 0

    #print("All check passed.")


@pytest.mark.parametrize(
    "http_request, fail, equal, expected_result",
    [
        (cf.ERROR_REQUEST, False, True, eq),
        (cf.ERROR_REQUEST, False, False, gt),
        (cf.ERRONEOUS_ERROR_REQUEST_1, True, None, cf.ERRONEOUS_ERROR_MSG_1),
        (cf.ERRONEOUS_ERROR_REQUEST_2, True, None, cf.ERRONEOUS_ERROR_MSG_2),
        (cf.ERRONEOUS_ERROR_REQUEST_3, True, None, cf.ERRONEOUS_ERROR_MSG_3),
    ],
)
def test_evaluate_forecast(
    initialise_clients, http_request, fail, equal, expected_result
):
    """
    Test if forecast errors are evaluated as expected

    Boolean flags:
        - fail: True if the test is expected to fail
        - equal: True if time series is compared with itself
    """

    # Get required clients from fixture
    sparql_client, ts_client, _, rdb_url = initialise_clients

    # Initialise all triples in test_triples + initialise time series in RDB
    cf.initialise_triples(sparql_client)
    cf.clear_database(rdb_url)
    ts_client.init_timeseries(dataIRI=cf.IRI_TO_FORECAST_1,
                              times=cf.TIMES, values=cf.VALUES_1,
                              ts_type=DOUBLE, time_format=TIME_FORMAT)
    ts_client.init_timeseries(dataIRI=cf.IRI_TO_FORECAST_2,
                              times=cf.TIMES, values=cf.VALUES_2,
                              ts_type=DOUBLE, time_format=TIME_FORMAT)

    if not fail:
        # Retrieve list of instantiated time series IRIs
        tsIRIs = sparql_client.get_all_tsIRIs()
        if equal:
            http_request['query']['tsIRI_target'] = tsIRIs[0]
            http_request['query']['tsIRI_fc'] = tsIRIs[0]
        else:
            http_request['query']['tsIRI_target'] = tsIRIs[0]
            http_request['query']['tsIRI_fc'] = tsIRIs[1]

    # Create HTTP request to evaluate forecast errors
    headers = {'Content-Type': 'application/json'}
    url = cf.AGENT_BASE_URL + '/evaluate_errors'
    response = requests.post(url, json=http_request, headers=headers)

    if fail:
        # Verify that correct error message is returned for erroneous requests
        assert response.status_code == 500
        assert expected_result in response.text

    else:
        # Check successful execution/response
        assert response.status_code == 200
        response = response.json()
        assert expected_result(response['mape'], 0)
        assert expected_result(response['smape'], 0)
        assert expected_result(response['mse'], 0)
        assert expected_result(response['rmse'], 0)
        assert expected_result(response['max_error'], 0)

        


# def test_load_pretrained_model():
#      """
#      Test the function `load_pretrained_model` to load a pretrained model from a
#      checkpoint file or a PyTorch model file
#      NOTE: Test will fail if the model is not available at the given link
#      """
#      # Test if pretrained model is loaded correctly
#      cfg = {
#           'model_configuration_name': 'test_model',
#           'fc_model': {
#                'name': 'TFTModel_test',
#                'model_path_ckpt_link': cf.DARTS_MODEL_OBJECT,
#                'model_path_pth_link': cf.DARTS_CHECKPOINTS,
#           },
#      }
#      model = load_pretrained_model(cfg, TFTModel, force_download=True)    
#      assert model.__class__.__name__ == 'TFTModel'
#      assert model.model.input_chunk_length == 168
#      assert model.model.output_chunk_length == 24
     
#      # use previously downloaded model
#      model = load_pretrained_model(cfg, TFTModel, force_download=False)    
#      assert model.__class__.__name__ == 'TFTModel'
#      assert model.model.input_chunk_length == 168
#      assert model.model.output_chunk_length == 24
