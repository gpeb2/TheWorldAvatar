from functools import cache
import logging
import os
from typing import Optional


from SPARQLWrapper import SPARQLWrapper, POST, JSON

logger = logging.getLogger(__name__)


class KgClient:
    def __init__(
        self, endpoint: str, user: Optional[str] = None, password: Optional[str] = None
    ):
        sparql = SPARQLWrapper(endpoint)
        sparql.setReturnFormat(JSON)
        if user is not None and password is not None:
            sparql.setCredentials(user=user, passwd=password)
        sparql.setMethod(POST)
        self.sparql = sparql

    def query(self, query: str):
        logger.info("Executing SPARQL query:\n" + query)
        self.sparql.setQuery(query)
        res = self.sparql.queryAndConvert()
        logger.info("Execution done")
        return res


@cache
def get_ontospecies_bgClient():
    return KgClient(os.environ["KG_ENDPOINT_ONTOSPECIES"])


@cache
def get_ontokin_bgClient():
    return KgClient(os.environ["KG_ENDPOINT_ONTOKIN"])


@cache
def get_ontocompchem_bgClient():
    return KgClient(os.environ["KG_ENDPOINT_ONTOCOMPCHEM"])


@cache
def get_ontozeolite_bgClient():
    return KgClient(os.environ["KG_ENDPOINT_ONTOZEOLITE"])


@cache
def get_sg_ontopClient():
    return KgClient(os.environ["KG_ENDPOINT_SG_ONTOP"])


@cache
def get_sgPlot_bgClient():
    return KgClient(os.environ["KG_ENDPOINT_SG_PLOT"])


@cache
def get_sgCompany_bgClient():
    return KgClient(os.environ["KG_ENDPOINT_SG_COMPANY"])


@cache
def get_sgDispersion_bgClient():
    return KgClient(os.environ["KG_ENDPOINT_SG_DISPERSION"])


@cache
def get_sgCarpark_bgClient():
    return KgClient(os.environ["KG_ENDPOINT_SG_CARPARK"])
