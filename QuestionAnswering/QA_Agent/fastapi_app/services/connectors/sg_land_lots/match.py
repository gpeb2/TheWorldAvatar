from typing import Annotated
from fastapi import Depends

from services.retrieve_docs import DocsRetriever, get_docs_retriever
from services.kg_client import KgClient
from .constants import LAND_USE_TYPES
from .kg_client import get_sg_land_lots_bg_client


class LandUseTypeMatcher:
    _REDIS_KEY_PREFIX = "sg_land_lots:land_use_types:"
    _INDEX_NAME = "idx:sg_land_lots:land_use_types_vss"

    def __init__(self, kg_client: KgClient, docs_retriever: DocsRetriever):
        self.kg_client = kg_client
        self.docs_retriever = docs_retriever

    def _query_data(self):
        query = """
SELECT ?IRI ?label ?comment WHERE {{
    VALUES ?IRI {{ {values} }}
    ?IRI rdfs:label ?label .
    ?IRI rdfs:comment ?comment .
}}""".format(
            values=" ".join(["<{iri}>".format(iri=iri) for iri in LAND_USE_TYPES])
        )
        return [
            {k: v["value"] for k, v in binding.items()}
            for binding in self.kg_client.query(query)["results"]["bindings"]
        ]

    def _linearize(self, datum: dict):
        return "label: {label}; comment: {comment}.".format(
            label=datum["label"], comment=datum["comment"]
        )

    def match(self, query):
        retrieved, _ = self.docs_retriever.retrieve(
            key="sg_land_lots:land_use_types",
            docs_getter=self._query_data,
            linearize_func=self._linearize,
            queries=[query],
            k=1,
        )[0][0]

        return retrieved["IRI"]


def get_land_use_type_matcher(
    kg_client: Annotated[KgClient, Depends(get_sg_land_lots_bg_client)],
    docs_retriever: Annotated[DocsRetriever, Depends(get_docs_retriever)]
):
    return LandUseTypeMatcher(
        kg_client=kg_client, docs_retriever=docs_retriever
    )
