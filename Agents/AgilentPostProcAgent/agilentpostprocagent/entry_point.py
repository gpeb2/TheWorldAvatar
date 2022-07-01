from pyderivationagent.conf import config_derivation_agent
from agilentpostprocagent.conf import config_agilent_postproc

from agilentpostprocagent.agent import AgilentPostProcAgent
from agilentpostprocagent.agent import default

import logging

# Avoid unnecessary logging information from py4j package
logging.getLogger("py4j").setLevel(logging.INFO)

def create_app():
    agent_config = config_derivation_agent()
    agilent_postproc_config = config_agilent_postproc()

    agent = AgilentPostProcAgent(
        register_agent=agilent_postproc_config.REGISTER_AGENT,
        agent_iri=agent_config.ONTOAGENT_SERVICE_IRI,
        time_interval=agent_config.DERIVATION_PERIODIC_TIMESCALE,
        derivation_instance_base_url=agent_config.DERIVATION_INSTANCE_BASE_URL,
        kg_url=agent_config.SPARQL_QUERY_ENDPOINT,
        kg_update_url=agent_config.SPARQL_UPDATE_ENDPOINT,
        kg_user=agent_config.KG_USERNAME,
        kg_password=agent_config.KG_PASSWORD,
        fs_url=agent_config.FILE_SERVER_ENDPOINT,
        fs_user=agent_config.FILE_SERVER_USERNAME,
        fs_password=agent_config.FILE_SERVER_PASSWORD,
        agent_endpoint=agent_config.ONTOAGENT_OPERATION_HTTP_URL,
        logger_name='prod'
    )

    agent.add_url_pattern('/', 'root', default, methods=['GET'])

    agent.register()
    agent.start_monitoring_derivations()
    return agent.app
