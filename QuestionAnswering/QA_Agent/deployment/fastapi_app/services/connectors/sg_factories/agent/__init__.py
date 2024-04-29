from collections import defaultdict
import logging
from typing import Annotated, Callable, Dict, List, Optional, Tuple

from fastapi import Depends

from model.constraint import ExtremeValueConstraint
from model.aggregate import AggregateOperator
from model.qa import QAData
from services.entity_store import EntityStore, get_entity_store
from utils.rdf import flatten_sparql_response
from core.kg import KgClient
from services.kg import get_sg_ontopClient
from ..model import FactoryAttrKey, FactoryNumAttrKey, Industry
from .make_sparql import SGFactoriesSPARQLMaker, get_sgFactories_sparqlmaker


logger = logging.getLogger(__name__)


class SGFactoriesAgent:
    def __init__(
        self,
        ontop_client: KgClient,
        entity_linker: EntityStore,
        sparql_maker: SGFactoriesSPARQLMaker,
    ):
        self.ontop_client = ontop_client
        self.entity_linker = entity_linker
        self.sparql_maker = sparql_maker

    def _lookup_company_names(self, factory_iris: List[str]):
        # TODO: cache mappings that have already been looked up before
        # and only query for unseen factories
        query = """PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX ontocompany: <http://www.theworldavatar.com/kg/ontocompany/>

SELECT ?CompanyLabel ?Factory WHERE {{
FILTER ( ?Factory IN ( {values} ) )
?Company ontocompany:isOwnerOf ?Factory .
?Company rdfs:label ?CompanyLabel .
}}""".format(
            values=", ".join("<{iri}>".format(iri=iri) for iri in factory_iris)
        )
        res = self.ontop_client.query(query)
        bindings = [
            {k: v["value"] for k, v in binding.items()}
            for binding in res["results"]["bindings"]
        ]

        factory2company = defaultdict(lambda: None)
        for binding in bindings:
            factory2company[binding["Factory"]] = binding["CompanyLabel"]

        return [factory2company[factory] for factory in factory_iris]

    def _add_factory_label(self, vars: List[str], bindings: List[dict]):
        try:
            iri_idx = vars.index("IRI")
        except ValueError:
            raise ValueError("IRI must be present, found: " + str(vars))

        vars.insert(iri_idx + 1, "FactoryName")
        for binding in bindings:
            binding["FactoryName"] = self.entity_linker.lookup_label(binding["IRI"])

    def _add_company_label(self, vars: List[str], bindings: List[dict]):
        try:
            iri_idx = vars.index("IRI")
        except ValueError:
            raise ValueError("IRI must be present, found: " + str(vars))

        vars.insert(iri_idx + 1, "Company")
        companies = self._lookup_company_names([binding["IRI"] for binding in bindings])
        for binding, company in zip(bindings, companies):
            binding["Company"] = company

    def lookup_factory_attribute(self, name: str, attr_key: FactoryAttrKey):
        iris = self.entity_linker.link(name, "Facility")

        query = self.sparql_maker.lookup_factory_attribute(iris=iris, attr_key=attr_key)
        res = self.ontop_client.query(query)

        vars, bindings = flatten_sparql_response(res)
        self._add_factory_label(vars, bindings)
        self._add_company_label(vars, bindings)

        return QAData(vars=vars, bindings=bindings)

    def _aggregate_across_industries(
        self, industry: Optional[Industry], query_maker: Callable[[Industry], str]
    ):
        industries = [industry] if industry else [i for i in Industry]

        vars = []
        vars_set = set()
        bindings = []

        for _industry in industries:
            query = query_maker(_industry)

            # TODO: Refactor to make a single call to database instead of for every industry value
            res = self.ontop_client.query(query)
            _vars, _bindings = flatten_sparql_response(res)

            _vars.insert(0, "Industry")
            for binding in _bindings:
                binding["Industry"] = _industry.value

            for var in _vars:
                if var not in vars_set:
                    vars.append(var)
                    vars_set.add(var)

            bindings.extend(_bindings)

        return vars, bindings

    def find_factories(
        self,
        industry: Optional[Industry] = None,
        numattr_constraints: Dict[FactoryNumAttrKey, ExtremeValueConstraint] = dict(),
        limit: Optional[int] = None,
    ):
        vars, bindings = self._aggregate_across_industries(
            industry=industry,
            query_maker=lambda _industry: self.sparql_maker.find_factories(
                industry=_industry, numattr_constraints=numattr_constraints, limit=limit
            ),
        )
        self._add_factory_label(vars, bindings)
        self._add_company_label(vars, bindings)

        return QAData(vars=vars, bindings=bindings)

    def count_factories(
        self,
        industry: Optional[Industry] = None,
    ):
        vars, bindings = self._aggregate_across_industries(
            industry=industry, query_maker=self.sparql_maker.count_factories
        )

        if industry is None and all(
            header in vars for header in ["FactoryCount", "CompanyCount"]
        ):
            bindings.append(
                {
                    "Industry": "SUM",
                    "FactoryCount": str(
                        sum(int(binding["FactoryCount"]) for binding in bindings)
                    ),
                    "CompanyCount": str(
                        sum(int(binding["CompanyCount"]) for binding in bindings)
                    ),
                }
            )

        return QAData(
            vars=vars,
            bindings=bindings,
        )

    def compute_aggregate_factory_attribute(
        self,
        attr_agg: Tuple[FactoryNumAttrKey, AggregateOperator],
        industry: Optional[Industry] = None,
    ):
        vars, bindings = self._aggregate_across_industries(
            industry=industry,
            query_maker=lambda _industry: self.sparql_maker.compute_aggregate_factory_attribute(
                industry=_industry, attr_agg=attr_agg
            ),
        )

        return QAData(
            vars=vars,
            bindings=bindings,
        )


def get_sgFactories_agent(
    ontop_client: Annotated[KgClient, Depends(get_sg_ontopClient)],
    entity_linker: Annotated[EntityStore, Depends(get_entity_store)],
    sparql_maker: Annotated[
        SGFactoriesSPARQLMaker, Depends(get_sgFactories_sparqlmaker)
    ],
):
    return SGFactoriesAgent(
        ontop_client=ontop_client,
        entity_linker=entity_linker,
        sparql_maker=sparql_maker,
    )
