from functools import cache
from typing import Annotated

from fastapi import Depends

from services.kg import KgClient, get_ontokin_bgClient
from services.stores.entity_store.base import IEntityLinker


class MechanismLinker(IEntityLinker):
    def __init__(self, bg_client: KgClient):
        self.bg_client = bg_client

    def link(self, text: str | None, **kwargs):
        if "provenance" not in kwargs:
            return []

        query = """PREFIX okin: <http://www.theworldavatar.com/ontology/ontokin/OntoKin.owl#>
PREFIX op: <http://www.theworldavatar.com/ontology/ontoprovenance/OntoProvenance.owl#>

SELECT DISTINCT *
WHERE {{
    ?Mechanism okin:hasProvenance/(op:hasDOI|op:hasURL) "{provenance}"
}}""".format(
            provenance=kwargs["provenance"]
        )

        _, bindings = self.bg_client.querySelectThenFlatten(query)
        return [binding["Mechanism"] for binding in bindings]


@cache
def get_mechanism_linker(bg_client: Annotated[KgClient, Depends(get_ontokin_bgClient)]):
    return MechanismLinker(bg_client=bg_client)


class ReactionLinker(IEntityLinker):
    def __init__(self, bg_client: KgClient):
        self.bg_client = bg_client

    def link(self, text: str | None, **kwargs) -> list[str]:
        if "equation" not in kwargs:
            return []

        query = """PREFIX okin: <http://www.theworldavatar.com/ontology/ontokin/OntoKin.owl#>
        
SELECT DISTINCT *
WHERE {{
    ?Reaction okin:hasEquation "{equation}"
}}""".format(
            equation=kwargs["equation"]
        )

        _, bindings = self.bg_client.querySelectThenFlatten(query)
        return [binding["Reaction"] for binding in bindings]


@cache
def get_reaction_linker(bg_client: Annotated[KgClient, Depends(get_ontokin_bgClient)]):
    return ReactionLinker(bg_client=bg_client)
