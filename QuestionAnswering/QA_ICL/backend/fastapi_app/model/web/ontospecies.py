from pydantic import BaseModel

from model.web.comp_op import ComparisonOperator
from model.kg.ontospecies import SpeciesIdentifierKey, SpeciesPropertyKey


class SpeciesRequest(BaseModel):
    chemical_class: list[str]
    use: list[str]
    identifier: dict[SpeciesIdentifierKey, str]
    property: dict[SpeciesPropertyKey, list[tuple[ComparisonOperator, float]]]
