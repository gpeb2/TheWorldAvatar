from types import NoneType, UnionType
from typing import (
    Any,
    TypeVar,
    get_origin,
    get_args,
)
from collections import defaultdict

from pydantic import TypeAdapter
from pydantic.fields import FieldInfo
from rdflib import URIRef

from model.rdf_orm import RDFEntity
from services.sparql import SparqlClient
from utils.itertools_recipes import batched


T = TypeVar("T", bound=RDFEntity)


def unpack_optional_type(annotation: type[Any]):
    if get_origin(annotation) is UnionType:
        args = get_args(annotation)
        return next(arg for arg in args if arg is not NoneType)
    else:
        return annotation


class RDFStore:
    BATCH_SIZE = 256

    def __init__(self, endpoint: str):
        self.sparql_client = SparqlClient(endpoint)

    def _get_many(self, T: type[T], iris: list[str] | tuple[str]):
        if not iris:
            empty_lst: list[T | None] = []
            return empty_lst

        query = """SELECT ?iri ?field ?value
WHERE {{
    VALUES ?iri {{ {iris} }}
    {triples}
}}""".format(
            iris=" ".join("<{iri}>".format(iri=iri) for iri in set(iris)),
            triples=" UNION ".join(
                """{{
    BIND ( "{field}" as ?field )
    ?iri {predicate} ?value .
}}""".format(
                    field=field, predicate=metadata["path"].n3()
                )
                for field, (_, metadata) in T.get_rdf_fields().items()
            ),
        )

        _, bindings = self.sparql_client.querySelectThenFlatten(query)
        field2iri2values: dict[str, defaultdict[str, list[str]]] = defaultdict(
            lambda: defaultdict(list)
        )
        for binding in bindings:
            field2iri2values[binding["field"]][binding["iri"]].append(binding["value"])

        def resolve_field_value(field: str, info: FieldInfo):
            annotation = info.annotation
            if annotation and get_origin(annotation) is list:
                t = get_args(annotation)[0]
                iri2values = field2iri2values[field]
                if issubclass(t, RDFEntity):
                    flattened = [v for values in iri2values.values() for v in values]
                    models = [x for x in self.get_many(t, flattened) if x]
                    count = 0
                    out: dict[str, list[RDFEntity]] = dict()
                    for iri, iri2values in iri2values.items():
                        out[iri] = models[count : count + len(iri2values)]
                        count += len(iri2values)
                    return out
                else:
                    return iri2values

            iri2values = field2iri2values[field]
            iri2value = {
                iri: values[0] if values else None for iri, values in iri2values.items()
            }

            if annotation:
                unpacked_type = unpack_optional_type(annotation)
                if issubclass(unpacked_type, RDFEntity):
                    models = self.get_many(unpacked_type, iri2value.values())
                    return {iri: model for iri, model in zip(iri2value.keys(), models)}
            return iri2value

        field2iri2data = {
            field: resolve_field_value(field, info)
            for field, (info, _) in T.get_rdf_fields().items()
        }

        iri2field2data: defaultdict[
            str, dict[str, RDFEntity | str | list[RDFEntity] | list[str]]
        ] = defaultdict(dict)
        for field, iri2data in field2iri2data.items():
            for iri, data in iri2data.items():
                iri2field2data[iri][field] = data

        adapter = TypeAdapter(list[T])
        models = adapter.validate_python(
            [{"IRI": iri, **field2data} for iri, field2data in iri2field2data.items()]
        )
        iri2model = {model.IRI: model for model in models}
        return [iri2model.get(iri) for iri in iris]

    def get_many(self, T: type[T], iris: list[str] | tuple[str]):
        return [
            x for batch in batched(iris, self.BATCH_SIZE) for x in self._get_many(T=T, iris=batch)
        ]

    def get_one(self, T: type[T], iri: str):
        return self.get_many(T, [iri])[0]

    def get_all(self, T: type[T], type_iri: URIRef):
        query = f"""SELECT DISTINCT ?IRI
WHERE {{
    ?IRI rdf:type {type_iri.n3()} .
}}"""
        _, bindings = self.sparql_client.querySelectThenFlatten(query)
        iris = [binding["IRI"] for binding in bindings]
        return [model for model in self.get_many(T, iris) if model]
