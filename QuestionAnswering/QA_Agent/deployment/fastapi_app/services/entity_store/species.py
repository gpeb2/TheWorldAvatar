from functools import cache
from typing import Annotated, List, Optional

from fastapi import Depends
from services.entity_store.base import IEntityLinker
from services.kg import KgClient, get_ontospecies_kgClient


class SpeciesLinker(IEntityLinker):
    IDKEY2PREDKEY = {"inchi": "InChI", "smiles": "SMILES", "iupac_name": "IUPACName"}

    def __init__(self, bg_client: KgClient):
        self.bg_client = bg_client

    def link(self, text: Optional[str], **kwargs) -> List[str]:
        for id_key, pred_key in self.IDKEY2PREDKEY.items():
            if id_key in kwargs:
                query = """PREFIX os: <http://www.theworldavatar.com/ontology/ontospecies/OntoSpecies.owl#>

SELECT ?Species WHERE {{
    ?Species a os:Species .
    ?Species os:has{pred_key}/os:value "{id_value}"
}}""".format(
                    pred_key=pred_key, id_value=kwargs[id_key]
                )

                iris = [
                    row["Species"]["value"]
                    for row in self.bg_client.query(query)["results"]["bindings"]
                ]
                if iris:
                    return iris

        texts = list(kwargs.values())
        texts.append(text)
        if not texts:
            return []

        query = """PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX os: <http://www.theworldavatar.com/ontology/ontospecies/OntoSpecies.owl#>

SELECT ?Species WHERE {{
    ?Species a os:Species .
    VALUES ?Text {{ {texts} }}
    ?Species (((os:hasIUPACName|os:hasMolecularFormula|os:hasSMILES)/os:value)|rdfs:label|skos:altLabel) ?Text .
}}""".format(
            texts=" ".join('"{val}"'.format(val=text) for text in texts)
        )

        return [
            row["Species"]["value"]
            for row in self.bg_client.query(query)["results"]["bindings"]
        ]


@cache
def get_species_linker(
    bg_client: Annotated[KgClient, Depends(get_ontospecies_kgClient)]
):
    return SpeciesLinker(bg_client=bg_client)
