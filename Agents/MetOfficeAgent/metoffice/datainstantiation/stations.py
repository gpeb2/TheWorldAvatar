###############################################
# Authors: Markus Hofmeister (mh807cam.ac.uk) #    
# Date: 01 Apr 2022                           #
###############################################

# The purpose of this module is to provide functions to retrieve 
# station data from the API and instantiate it in the KG

import uuid

import agentlogging
from metoffice.utils.properties import QUERY_ENDPOINT, UPDATE_ENDPOINT
from metoffice.kgutils.kgclient import KGClient
from metoffice.kgutils.prefixes import create_sparql_prefix
from metoffice.kgutils.prefixes import PREFIXES
from metoffice.kgutils.querytemplates import *


# Initialise logger
logger = agentlogging.get_logger("dev")


def instantiate_stations(station_data: list,
                         query_endpoint: str = QUERY_ENDPOINT,
                         update_endpoint: str = UPDATE_ENDPOINT) -> None:
    """
        Instantiates a list of measurement stations in a single SPARQL update;
        Arguments:
            station_data - list of dictionaries with station parameters as returned
                           from MetOffice DataPoint using metoffer
    """
    
    # Initialise update query
    query_string = f"""
        {create_sparql_prefix('geo')}
        {create_sparql_prefix('rdf')}
        {create_sparql_prefix('rdfs')}
        {create_sparql_prefix('xsd')}
        {create_sparql_prefix('ems')}
        {create_sparql_prefix('kb')}
        INSERT DATA {{
    """

    # Add station details
    for data in station_data:
        station_IRI = PREFIXES['kb'] + 'ReportingStation_' + str(uuid.uuid4())
        # Extract station information from API result
        to_instantiate = _condition_metoffer_data(data)
        to_instantiate['station_iri'] = station_IRI
        query_string += add_station_data(**to_instantiate)

    # Close query
    query_string += f"}}"

    # Execute query
    kg_client = KGClient(query_endpoint, update_endpoint)
    kg_client.performUpdate(query_string)



def _condition_metoffer_data(station_data: dict) -> dict:
    """
        Condition retrieved MetOffer data as required for query template
        Arguments:
            station_data - station data dict as returned by MetOffer
    """

    # Data format as required for "add_station_data" query template
    conditioned = {'dataSource': "Met Office DataPoint",
                   'id': None,
                   'comment': None,
                   'location': None,
                   'elevation': None
    }
    # Extract relevant data
    if 'id' in station_data.keys(): conditioned['id'] = station_data['id']
    if 'name' in station_data.keys(): conditioned['comment'] = station_data['name']
    if 'elevation' in station_data.keys(): conditioned['elevation'] = station_data['elevation']
    if ('latitude' in station_data.keys()) and ('longitude' in station_data.keys()):
        conditioned['location'] = station_data['latitude'] + '#' + station_data['longitude']
    else:
        logger.warning(f"Station {station_data['id']} does not have location data.")
    
    return conditioned
