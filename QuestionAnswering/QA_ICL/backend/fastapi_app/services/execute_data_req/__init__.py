from functools import cache
from typing import Annotated

from fastapi import Depends

from model.nlq2datareq import (
    DataRequestForm,
    FuncDataReqForm,
    SparqlDataReqForm,
)

from .func import FuncDataReqExecutor, get_funcReq_executor
from .sparql import SparqlDataReqExecutor, get_sparqlReq_executor


class DataReqExecutor:
    def __init__(
        self, sparql_executor: SparqlDataReqExecutor, func_executor: FuncDataReqExecutor
    ):
        self.sparql_executor = sparql_executor
        self.func_executor = func_executor

    def exec(
        self,
        var2cls: dict[str, str],
        entity_bindings: dict[str, list[str]],
        const_bindings: dict[str, object],
        req_form: DataRequestForm,
    ):
        if isinstance(req_form, SparqlDataReqForm):
            exec_func = self.sparql_executor.exec
        elif isinstance(req_form, FuncDataReqForm):
            exec_func = self.func_executor.exec
        else:
            exec_func = lambda _: ([], None)

        return exec_func(
            var2cls=var2cls,
            entity_bindings=entity_bindings,
            const_bindings=const_bindings,
            req_form=req_form,
        )


@cache
def get_dataReq_executor(
    sparql_executor: Annotated[SparqlDataReqExecutor, Depends(get_sparqlReq_executor)],
    func_executor: Annotated[FuncDataReqExecutor, Depends(get_funcReq_executor)],
):
    return DataReqExecutor(sparql_executor=sparql_executor, func_executor=func_executor)
