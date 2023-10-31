from typing import Iterable

PRINT_TEMPLATE = "{classname}[{properties}]"

class _Repr:
    def __repr__(self):
        return PRINT_TEMPLATE.format(classname=type(self).__name__, properties=self.__dict__.__repr__())

class SelectClause(_Repr):
    def __init__(self, vars: Iterable[str]):
        self.vars = list(vars)

class GraphPattern(_Repr):
    def __repr__(self):
        return PRINT_TEMPLATE.format(classname=type(self).__name__, properties=self.__dict__.__repr__())

class ValuesClause(GraphPattern):
    def __init__(self, var: str, values: Iterable[str]):
        self.var = var
        self.values = list(values)

class FilterClause(GraphPattern):
    def __init__(self, constraint: str):
        self.constraint = constraint

class TriplePattern(GraphPattern):
    def __init__(self, s: str, p: str, o: str):
        self.s = s
        self.p = p
        self.o = o

class SparqlCompactRep(_Repr):
    def __init__(self, select_clause: SelectClause, graph_patterns: Iterable[GraphPattern]):
        self.select_clause = select_clause
        self.graph_patterns = graph_patterns

    @classmethod
    def _extract_select_clause(cls, sparql_compact: str):
        """sparql_compact: SELECT ?x WHERE {graph_patterns...}"""
        sparql_compact = sparql_compact.strip()
        select_clause, sparql_compact = sparql_compact.split("WHERE", maxsplit=1)
        select_clause = select_clause.strip()

        assert select_clause.startswith("SELECT")
        vars = select_clause[len("SELECT"):].strip().split()
        select_clause = SelectClause(vars)

        sparql_compact = sparql_compact.strip()
        assert sparql_compact.startswith("{"), sparql_compact
        assert sparql_compact.endswith("}"), sparql_compact
        graph_patterns_str = sparql_compact[1:-1]

        return select_clause, graph_patterns_str

    @classmethod
    def _extract_values_clause(cls, graph_patterns_str: str):
        """VALUES ?Species { {literal} {literal} ... }"""
        graph_patterns_str = graph_patterns_str[len("VALUES") :].strip()
        assert graph_patterns_str.startswith("?Species"), graph_patterns_str
        graph_patterns_str = graph_patterns_str[len("?Species") :].strip()
        assert graph_patterns_str.startswith("{"), graph_patterns_str
        graph_patterns_str = graph_patterns_str[1:].strip()

        ptr = 0
        literals = []
        while ptr < len(graph_patterns_str) and graph_patterns_str[ptr] != '}':
            assert graph_patterns_str[ptr] == '"', graph_patterns_str
            _ptr = ptr + 1
            _ptr_literal_start = _ptr
            while _ptr < len(graph_patterns_str) and graph_patterns_str[_ptr] != '"':
                _ptr += 1
            assert graph_patterns_str[_ptr] == '"'

            literal = graph_patterns_str[_ptr_literal_start: _ptr]
            literals.append(literal)

            ptr = _ptr + 1
            while ptr < len(graph_patterns_str) and graph_patterns_str[ptr].isspace():
                ptr += 1

        values_clause = ValuesClause("?Species", literals)
        graph_patterns_str = graph_patterns_str[ptr + 1:]
        
        return graph_patterns_str, values_clause
    
    @classmethod
    def _extract_filter_clause(cls, graph_patterns_str: str):
        graph_patterns_str = graph_patterns_str[len("FILTER") :].strip()
        assert graph_patterns_str.startswith("("), graph_patterns_str

        graph_patterns_str = graph_patterns_str[1:].strip()
        ptr = 0
        quote_open = False
        while ptr < len(graph_patterns_str) and (
            graph_patterns_str[ptr] != ")" or quote_open
        ):
            if graph_patterns_str[ptr] == '"':
                quote_open = not quote_open
            ptr += 1
        assert graph_patterns_str[ptr] == ")", graph_patterns_str

        constraint = graph_patterns_str[:ptr].strip()
        filter_clause = FilterClause(constraint)

        graph_patterns_str = graph_patterns_str[ptr + 1 :]

        return graph_patterns_str, filter_clause

    @classmethod
    def _extract_triple_pattern(cls, graph_patterns_str: str):
        subj, predicate, graph_patterns_str = graph_patterns_str.split(maxsplit=2)

        graph_patterns_str = graph_patterns_str.strip()
        if graph_patterns_str.startswith('"'):
            ptr = 1
            while ptr < len(graph_patterns_str) and graph_patterns_str[ptr] != '"':
                ptr += 1
            obj = graph_patterns_str[: ptr + 1]
            graph_patterns_str = graph_patterns_str[ptr + 1 :].strip()
            assert graph_patterns_str.startswith("."), graph_patterns_str
            graph_patterns_str = graph_patterns_str[1:]
        else:
            obj, graph_patterns_str = graph_patterns_str.split(maxsplit=1)
            if obj.endswith("."):
                obj = obj[:-1]
            else:
                graph_patterns_str = graph_patterns_str.strip()
                assert graph_patterns_str.startswith("."), graph_patterns_str
                graph_patterns_str = graph_patterns_str[1:]

        triple_pattern = TriplePattern(subj, predicate, obj)

        return graph_patterns_str, triple_pattern

    @classmethod
    def fromstring(cls, sparql_compact: str):
        select_clause, graph_patterns_str = cls._extract_select_clause(sparql_compact)

        graph_patterns = []
        while len(graph_patterns_str) > 0:
            graph_patterns_str = graph_patterns_str.strip()
            if graph_patterns_str.startswith("VALUES"):
                graph_patterns_str, pattern = cls._extract_values_clause(graph_patterns_str)
            elif graph_patterns_str.startswith("FILTER"):
                graph_patterns_str, pattern = cls._extract_filter_clause(graph_patterns_str)
            else:
                graph_patterns_str, pattern = cls._extract_triple_pattern(graph_patterns_str)
            graph_patterns.append(pattern)

        return SparqlCompactRep(select_clause, graph_patterns)
