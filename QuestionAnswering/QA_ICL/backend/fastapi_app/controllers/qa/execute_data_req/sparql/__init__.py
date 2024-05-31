from functools import cache
import logging
from typing import Annotated, Any, Dict, List

from fastapi import Depends

from constants.prefixes import PREFIX_NAME2URI
from services.example_store.model import SparqlDataReqForm
from services.kg import KgClient
from services.model import DocumentCollectionDataItem, TableDataItem
from utils.collections import FrozenDict
from utils.json import flatten_dict
from utils.rdf import flatten_sparql_select_response
from .process_query import SparqlQueryProcessor, get_sparqlQuery_processor
from .kg import get_ns2kg
from .transform_response import (
    SparqlResponseTransformer,
    get_sparqlRes_transformer,
)


logger = logging.getLogger(__name__)


class SparqlDataReqExecutor:
    PREFIXES = (
        "\n".join(
            "PREFIX {name}: <{uri}>".format(name=name, uri=uri)
            for name, uri in PREFIX_NAME2URI.items()
        )
        + "\n"
    )

    def __init__(
        self,
        ns2kg: Dict[str, KgClient],
        query_processor: SparqlQueryProcessor,
        response_processor: SparqlResponseTransformer,
    ):
        self.ns2kg = ns2kg
        self.query_processor = query_processor
        self.response_processor = response_processor

    def exec(
        self,
        entity_bindings: Dict[str, List[str]],
        const_bindings: Dict[str, Any],
        req_form: SparqlDataReqForm,
    ):
        logger.info("Unprocessed query:\n" + req_form.query)
        query = self.query_processor.process(
            sparql=req_form.query,
            entity_bindings=entity_bindings,
            const_bindings=const_bindings,
        )
        logger.info("Processed query:\n" + query)

        prefixed_query = self.PREFIXES + query
        logger.info(
            "Executing query at: " + self.ns2kg[req_form.namespace].sparql.endpoint
        )
        res = self.ns2kg[req_form.namespace].querySelect(prefixed_query)
        vars, bindings = flatten_sparql_select_response(res)

        logger.info("Transforming SPARQL response to documents...")
        docs = self.response_processor.transform(
            vars=vars, bindings=bindings, res_map=req_form.res_map
        )
        docs_item = DocumentCollectionDataItem(data=docs)
        logger.info("Done")

        logger.info("Linearising documents into a table...")
        flattened_docs = [flatten_dict(doc) for doc in docs]
        table_item = TableDataItem.from_data(flattened_docs)
        logger.info("Done")

        return [docs_item, table_item]


@cache
def get_sparqlReq_executor(
    ns2kg: Annotated[FrozenDict[str, KgClient], Depends(get_ns2kg)],
    query_processor: Annotated[
        SparqlQueryProcessor, Depends(get_sparqlQuery_processor)
    ],
    response_processor: Annotated[
        SparqlResponseTransformer, Depends(get_sparqlRes_transformer)
    ],
):
    return SparqlDataReqExecutor(
        ns2kg=ns2kg,
        query_processor=query_processor,
        response_processor=response_processor,
    )
