from functools import cache
import logging
import time
from typing import Annotated, Dict, List

from fastapi import Depends

from model.qa import QAStep
from core.kg import KgClient
from services.support_data import TableDataItem
from utils.collections import FrozenDict
from utils.rdf import flatten_sparql_response
from ..model import SparqlAction
from .process_query import SparqlQueryProcessor, get_sparqlQuery_processor
from .kg import get_ns2kg
from .process_response import (
    SparqlResponseProcessor,
    get_sparqlRes_processor,
)


logger = logging.getLogger(__name__)


class SparqlActionExecutor:
    PREFIXES = """PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>

PREFIX unit: <http://qudt.org/vocab/unit/>
PREFIX om: <http://www.ontology-of-units-of-measure.org/resource/om-2/>
PREFIX bldg: <http://www.opengis.net/citygml/building/2.0/>
PREFIX geo: <http://www.opengis.net/ont/geosparql#>
PREFIX geof: <http://www.opengis.net/def/function/geosparql/>
PREFIX gml: <http://www.opengis.net/citygml/building/2.0/>
PREFIX grp: <http://www.opengis.net/citygml/cityobjectgroup/2.0/>

PREFIX bs: <https://www.theworldavatar.com/kg/ontobuildingstructure/>
PREFIX carpark:	<https://www.theworldavatar.com/kg/ontocarpark/>
PREFIX landplot: <https://www.theworldavatar.com/kg/landplot/>
PREFIX ontobim: <https://www.theworldavatar.com/kg/ontobim/>
PREFIX obe: <https://www.theworldavatar.com/kg/ontobuiltenv/>
PREFIX ontocompany: <http://www.theworldavatar.com/kg/ontocompany/>
PREFIX ontochemplant: <http://www.theworldavatar.com/kg/ontochemplant/>
PREFIX ontoplanreg: <https://www.theworldavatar.com/kg/ontoplanningregulation/>
PREFIX ontoplot: <https://www.theworldavatar.com/kg/ontoplot/>
PREFIX ontozoning: <https://www.theworldavatar.com/kg/ontozoning/>
PREFIX ub: <https://www.theworldavatar.com/kg/ontoubemmp/>

"""

    def __init__(
        self,
        ns2kg: Dict[str, KgClient],
        query_processor: SparqlQueryProcessor,
        response_processor: SparqlResponseProcessor,
    ):
        self.ns2kg = ns2kg
        self.query_processor = query_processor
        self.response_processor = response_processor

    def exec(self, action: SparqlAction):
        steps: List[QAStep] = []

        logger.info("Input query:\n" + action.query)
        timestamp = time.time()
        query = self.query_processor.process(action.query)
        latency = time.time() - timestamp
        logger.info("Processed query:\n" + query)
        steps.append(
            QAStep(
                action="postprocess_sparql",
                arguments=action.query,
                results=query,
                latency=latency,
            )
        )

        prefixed_query = self.PREFIXES + query
        logger.info(
            "Executing query at: " + self.ns2kg[action.namespace].sparql.endpoint
        )
        timestamp = time.time()
        res = self.ns2kg[action.namespace].query(prefixed_query)
        latency = time.time() - timestamp
        steps.append(
            QAStep(
                action="execute_sparql",
                arguments=dict(namespace=action.namespace, query=query),
                latency=latency,
            )
        )

        vars, bindings = flatten_sparql_response(res)
        self.response_processor.process(vars=vars, bindings=bindings)

        return steps, [TableDataItem(vars=vars, bindings=bindings)]


@cache
def get_sparqlAction_executor(
    ns2kg: Annotated[FrozenDict[str, KgClient], Depends(get_ns2kg)],
    query_processor: Annotated[
        SparqlQueryProcessor, Depends(get_sparqlQuery_processor)
    ],
    response_processor: Annotated[
        SparqlResponseProcessor, Depends(get_sparqlRes_processor)
    ],
):
    return SparqlActionExecutor(
        ns2kg=ns2kg,
        query_processor=query_processor,
        response_processor=response_processor,
    )
