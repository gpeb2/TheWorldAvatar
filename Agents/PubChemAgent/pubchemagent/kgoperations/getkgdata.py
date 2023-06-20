from pubchemagent.kgoperations.queryendpoints import SPARQL_ENDPOINTS
from pubchemagent.kgoperations.querykg import kg_operations
from pubchemagent.kgoperations.querytemplates import *


def get_iri_data(inchi):
    inchi_string=inchi
    query = get_iri_query(inchi_string)
    sparqlendpoint = SPARQL_ENDPOINTS['copyontospecies']
    # create a SPARQL object for performing the query
    kg_client = kg_operations(sparqlendpoint)
    data = kg_client.querykg(queryStr=query)
    iri = None
    flag = "true"
    if data:
        data = data[0]
        iri = data['speciesIRI']
        flag = "true"

    inchi_string = inchi_string.replace("=1S/", "=1/")
    query = get_iri_query(inchi_string)
    data = kg_client.querykg(query)
    if data:
        data = data[0]
        iri = data['speciesIRI']
        flag = "true"
    
    inchi_string = inchi_string.replace("=1/", "=1S/")
    query = spec_inchi_query(inchi_string)
    sparqlendpoint = SPARQL_ENDPOINTS['pubchem']
    # create a SPARQL object for performing the query
    kg_client = kg_operations(sparqlendpoint)
    data = kg_client.querykg(query)
    if data:
        data = data[0]
        iri = data['speciesIRI']
        flag = "false"

    return iri, flag

def get_uuid(typeIRI, string):
    # query = get_iri_query(inchi_string=inchi)
    query = generic_query(typeIRI, string)
    sparqlendpoint = SPARQL_ENDPOINTS['pubchem']
    # create a SPARQL object for performing the query
    kg_client = kg_operations(sparqlendpoint)
    data = kg_client.querykg(queryStr=query)
    iri = None
    if data:
        data = data[0]
        iri = data['IRI']
    return iri

def get_ref_uuid(ref_value, ref_unit_uuid):
    # query = get_iri_query(inchi_string=inchi)
    query = ref_state_query(ref_value, ref_unit_uuid)
    sparqlendpoint = SPARQL_ENDPOINTS['pubchem']
    # create a SPARQL object for performing the query
    kg_client = kg_operations(sparqlendpoint)
    data = kg_client.querykg(queryStr=query)
    iri = None
    if data:
        data = data[0]
        iri = data['IRI']
    return iri

def get_element_IRI(symbol):
    # query = get_iri_query(inchi_string=inchi)
    query = element_query(symbol)
    sparqlendpoint = SPARQL_ENDPOINTS['pubchem']
    # create a SPARQL object for performing the query
    kg_client = kg_operations(sparqlendpoint)
    data = kg_client.querykg(queryStr=query)
    iri = None
    if data:
        data = data[0]
        iri = data['IRI']
    return iri