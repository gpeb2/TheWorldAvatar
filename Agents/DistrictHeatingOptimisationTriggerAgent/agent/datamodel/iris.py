# Provide Constants/IRIs for KG queries/updates

 # Namespaces
# External ontologies
RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
XSD = "http://www.w3.org/2001/XMLSchema#"
TIME = "http://www.w3.org/2006/time#"
# CoMo / CARES ontologies
OD =  'https://www.theworldavatar.com/kg/ontodispersion/'
TS =  'https://www.theworldavatar.com/kg/ontotimeseries/'
OHN = 'https://www.theworldavatar.com/kg/ontoheatnetwork/'
EMS = "https://www.theworldavatar.com/kg/ontoems/"
KB = 'https://www.theworldavatar.com/kg/pms_dh/'
# Derivation markup
DERIVATION_INSTANCE_BASE_URL = 'https://www.theworldavatar.com/kg/derivation/'

# Forecasting model instances #
# NOTE: to be aligned with fcmodels.ttl
fc_model_heat_demand = KB + 'ForecastingModel_TFT_heat_demand'
fc_model_grid_temperature = KB + 'ForecastingModel_Prophet'

### Concept/Data types ###
RDF_TYPE = RDF + 'type'
XSD_DECIMAL = XSD + 'decimal'

### OntoDispersion ###
OD_SIMULATION_TIME = OD + 'SimulationTime'

# OntoTimeSeries
TS_FREQUENCY = TS + 'Frequency'
TS_FORECASTINGMODEL = TS + "ForecastingModel"
TS_HASCOVARIATE = TS + "hasCovariate"

### Time ontology ###
TIME_INTERVAL = TIME + 'Interval'
TIME_HASBEGINNING = TIME + 'hasBeginning'
TIME_HASEND = TIME + 'hasEnd'
TIME_INSTANT = TIME + 'Instant'
TIME_INTIMEPOSITION = TIME + 'inTimePosition'
TIME_TIMEPOSITION = TIME + 'TimePosition'
TIME_HASTRS = TIME + 'hasTRS'
TIME_NUMERICPOSITION = TIME + 'numericPosition'
UNIX_TIME = "http://dbpedia.org/resource/Unix_time"
TIME_DURATION = TIME + 'Duration'
TIME_NUMERICDURATION = TIME + 'numericDuration'
TIME_UNIT_TYPE = TIME + 'unitType'
TIME_UNIT_DAY = TIME + 'unitDay'
TIME_UNIT_HOUR = TIME + 'unitHour'
TIME_UNIT_MINUTE = TIME + 'unitMinute'
TIME_UNIT_SECOND = TIME + 'unitSecond'

### ONTOEMS ###
EMS_AIRTEMPERATURE = EMS + "AirTemperature"

### OHN ###
OHN_ISPUBLICHOLIDAY = OHN + "isPublicHoliday"