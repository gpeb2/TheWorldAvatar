from typing import Annotated, Optional

from fastapi import Depends
from services.stores.entity_store.base import IEntityLinker
from services.kg import KgClient, get_ontozeolite_bgClient


class ZeoliticMaterialLinker(IEntityLinker):
    def __init__(self, bg_client: KgClient):
        self.bg_client = bg_client

    def link(self, text: Optional[str], **kwargs):
        if "formula" not in kwargs:
            return []

        query = """PREFIX zeo: <http://www.theworldavatar.com/kg/ontozeolite/>
SELECT ?Material
WHERE {{
    ?Material zeo:hasChemicalFormula "{formula}" .
}}""".format(
            formula=kwargs["formula"]
        )

        _, bindings = self.bg_client.querySelectThenFlatten(query)
        return [row["Material"] for row in bindings]


def get_zeoliticMaterial_linker(
    bg_client: Annotated[KgClient, Depends(get_ontozeolite_bgClient)]
):
    return ZeoliticMaterialLinker(bg_client=bg_client)
