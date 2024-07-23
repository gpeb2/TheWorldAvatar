import logging
from typing import Annotated, get_args

from fastapi import APIRouter, Depends, HTTPException, Query, Request

from model.kg.ontozeolite import (
    OntozeoliteZeoliteFramework,
    OntozeoliteZeoliteFrameworkBase,
)
from model.web.ontozeolite import (
    CrystalInfoRequest,
    ScalarTopologicalPropertyKey,
    TopoPropsRequest,
    UnitCellKey,
    XRDPeakRequest,
    ZeoliteFrameworkRequest,
)
from routers.ontozeolite.base import CIFResponse
from services.mol_vis.cif import CIFManager, get_cif_manager
from services.rdf_stores.ontozeolite import (
    OntozeoliteRDFStore,
    get_ontozeolite_rdfStore,
)
from routers.utils import parse_rhs_colon


logger = logging.getLogger(__name__)

router = APIRouter()


UNIT_CELL_QUERY_KEYS = {
    f"unit-cell-{key.value}": key for cls in get_args(UnitCellKey) for key in cls
}


async def parse_zeolite_frameworks_request(
    req: Request,
    xrd_peak: Annotated[
        list[str],
        Query(
            ...,
            alias="xrd-peak",
            description="URL-encoded JSON object with keys `position` (required), `width` (optional, defaults to `0.5`), `threshold` (optional, defaults to `50) that describe an XRD peak. All keys are optional.",
        ),
    ] = [],
    composite_bu: Annotated[list[str], Query(..., alias="composite-bu")] = [],
    secondary_bu: Annotated[list[str], Query(..., alias="secondary-bu")] = [],
):
    return ZeoliteFrameworkRequest(
        crystal_info=CrystalInfoRequest(
            xrd_peak=[XRDPeakRequest.model_validate_json(x) for x in xrd_peak],
            unit_cell={
                py_key: [
                    parse_rhs_colon(val) for val in req.query_params.getlist(query_key)
                ]
                for query_key, py_key in UNIT_CELL_QUERY_KEYS.items()
                if query_key in req.query_params
            },
        ),
        topo_props=TopoPropsRequest(
            scalars={
                key: [
                    parse_rhs_colon(val) for val in req.query_params.getlist(key.value)
                ]
                for key in get_args(ScalarTopologicalPropertyKey)
                if key.value in req.query_params
            },
            composite_bu=composite_bu,
            secondary_bu=secondary_bu,
        ),
        return_fields=None,
    )


@router.get(
    "/",
    summary="Get zeolite frameworks",
    openapi_extra={
        "parameters": [
            *(
                {
                    "in": "query",
                    "name": name,
                    "schema": {
                        "type": "string",
                    },
                }
                for name in UNIT_CELL_QUERY_KEYS.keys()
            ),
            *(
                {
                    "in": "query",
                    "name": key.value,
                    "schema": {"type": "array", "items": {"type": "string"}},
                    "description": "RHS colon filters e.g. `eq:100`, `lte:200`",
                }
                for key in get_args(ScalarTopologicalPropertyKey)
            ),
        ]
    },
    response_model=list[OntozeoliteZeoliteFrameworkBase],
)
async def getZeoliteFrameworks(
    framework_req: Annotated[
        ZeoliteFrameworkRequest, Depends(parse_zeolite_frameworks_request)
    ],
    ontozeolite_store: Annotated[
        OntozeoliteRDFStore, Depends(get_ontozeolite_rdfStore)
    ],
):
    print(framework_req)
    return ontozeolite_store.get_zeolite_frameworks(framework_req)


@router.get(
    "/{iri:path}/cif",
    summary="Get zeolite's CIF geometry file",
    response_class=CIFResponse,
)
async def getZeoliteFrameworkCIF(
    iri: str, cif_manager: Annotated[CIFManager, Depends(get_cif_manager)]
):
    cif = cif_manager.get([iri])[0]
    if not cif:
        raise HTTPException(
            status_code=404, detail=f"CIF not found for zeolite `{iri}`"
        )
    return CIFResponse(
        content=cif,
        headers={"Content-Disposition": 'attachment; filename="zeolite.cif"'},
    )


@router.get(
    "/{iri:path}",
    summary="Get zeolite framework",
    response_model=OntozeoliteZeoliteFramework,
    response_model_exclude_none=True,
)
async def getZeoliteFrameworkOne(
    iri: str,
    ontozeolite_store: Annotated[
        OntozeoliteRDFStore, Depends(get_ontozeolite_rdfStore)
    ],
):
    return ontozeolite_store.get_zeolite_framework_one(iri)
