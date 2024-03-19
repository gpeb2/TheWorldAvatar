from functools import cache
import os

from services.kg_client import KgClient


@cache
def get_sg_factories_bg_client():
    return KgClient(os.getenv("KG_ENDPOINT_SG_FACTORIES", "localhost"))


@cache
def get_sg_factories_ontop_client():
    return KgClient(os.getenv("KG_ENDPOINT_SG_FACTORIES_ONTOP", "localhost"))
