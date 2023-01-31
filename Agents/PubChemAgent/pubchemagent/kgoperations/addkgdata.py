from pubchemagent.kgoperations.queryendpoints import SPARQL_ENDPOINTS
from pubchemagent.kgoperations.querykg import kg_operations
from pubchemagent.kgoperations.querytemplates import *
from pubchemagent.kgoperations.getkgdata import *
import uuid
import re
   
# a sample data addition function
def insert_ontospecies(typeIRI, type, uuid, data):
    prev_key = ''
    for item in data:
        insert_str = ''
        if data[item].get('key') == prev_key:
            i = i+1 
        else:
            i = 1
        if data[item].get('reference'):
            prov_IRI = '<http://www.theworldavatar.com/ontology/ontokin/OntoKin.owl#Reference>'
            prov_uuid = find_uuid('Reference' , prov_IRI, data[item].get('reference'))
        else:
            prov_IRI = '<http://www.theworldavatar.com/ontology/ontokin/OntoKin.owl#Reference>'
            prov_uuid = find_uuid('Reference' , prov_IRI, '')
        if data[item].get('type')=='identifier':
            insert_str = pubchem_id_insert(typeIRI, type, uuid, i,  prov_uuid, data[item])
        elif data[item].get('type') in {'num_prop', 'thermo_prop'}:
            if 'unit' in data[item].get('value'):
                unit_IRI = '<http://www.ontology-of-units-of-measure.org/resource/om-2/Unit>'
                unit_string = str(data[item].get('value').get('unit'))
                unit_string=re.sub("°","deg",unit_string)
                unit_uuid = find_uuid('Unit' , unit_IRI, unit_string)
                if data[item].get('type')=='num_prop':
                    insert_str = pubchem_num_prop_insert(typeIRI, type, uuid, i, prov_uuid, unit_uuid, data[item])
                elif data[item].get('type')=='thermo_prop':
                    ref_unit_string = str(data[item].get('value').get('ref_unit'))
                    ref_unit_string=re.sub("°","deg",ref_unit_string)
                    ref_unit_uuid = find_uuid('Unit' , unit_IRI, ref_unit_string)
                    insert_str = pubchem_thermo_prop_insert(typeIRI, type, uuid, i, prov_uuid, unit_uuid, ref_unit_uuid, data[item])
        elif data[item].get('type') == 'string_prop':
            insert_str = pubchem_string_prop_insert(typeIRI, type, uuid, i, prov_uuid, data[item])
        elif data[item].get('type') == 'classification':
            classificationIRI = '<http://www.theworldavatar.com/ontology/ontospecies/OntoSpecies.owl#' + data[item].get('key') + '>'
            classification_uuid = find_uuid(data[item].get('key'), classificationIRI, data[item].get('value'), data[item].get('description') )
            insert_str = pubchem_classification_insert(typeIRI, type, uuid, i, prov_uuid, classification_uuid, data[item])
        elif data[item].get('type') == 'use':
            useIRI = '<http://www.theworldavatar.com/ontology/ontospecies/OntoSpecies.owl#' + data[item].get('key') + '>'
            use_uuid = find_uuid(data[item].get('key'), useIRI, data[item].get('value'))
            insert_str = pubchem_use_insert(typeIRI, type, uuid, i, prov_uuid, use_uuid, data[item])

        prev_key = data[item].get('key')

        if insert_str != '':
            sparqlendpoint = SPARQL_ENDPOINTS['pubchem']
            # create a SPARQL object for performing the query
            kg_client = kg_operations(sparqlendpoint)
            kg_client.insertkg(insertStr=insert_str)

def insert_structure(uuid, geometry, bonds):

    sparqlendpoint = SPARQL_ENDPOINTS['pubchem']
    # create a SPARQL object for performing the query
    kg_client = kg_operations(sparqlendpoint)

    geomIRI = '<http://www.theworldavatar.com/kg/ontospecies/Geometry_1_Species_' + uuid + '>'

    prov_IRI = '<http://www.theworldavatar.com/ontology/ontokin/OntoKin.owl#Reference>'
    prov_uuid = find_uuid('Reference' , prov_IRI, 'https://pubchem.ncbi.nlm.nih.gov')

    unit_IRI = '<http://www.ontology-of-units-of-measure.org/resource/om-2/Unit>'
    unit_uuid = find_uuid('Unit' , unit_IRI, 'angstrom')

    for item in geometry:
        elementIRI = get_element_IRI(geometry[item].get('element'))
        geometry[item]['element']=elementIRI
    
    insert_str = pubchem_atom_insert_2(uuid, geomIRI, prov_uuid, unit_uuid, geometry)
    kg_client.insertkg(insertStr=insert_str)

    for item in bonds:
        insert_str = pubchem_bond_insert(uuid, item+1, bonds[item])
        kg_client.insertkg(insertStr=insert_str)

def insert_spectra(uuid, data):
    prev_key = ''
    for item in data:
        if data[item].get('key') == prev_key:
            i = i+1 
        else:
            i = 1

        prov_uuid = ''
        solvent_uuid = ''
        unit_uuid = ''
        it_uuid = ''
        im_uuid = ''
        # check for reference
        if data[item].get('reference'):
            prov_IRI = '<http://www.theworldavatar.com/ontology/ontokin/OntoKin.owl#Reference>'
            prov_uuid = find_uuid('Reference' , prov_IRI, data[item].get('reference'))
        else:
            prov_IRI = '<http://www.theworldavatar.com/ontology/ontokin/OntoKin.owl#Reference>'
            prov_uuid = find_uuid('Reference' , prov_IRI, '', 'data without reference')

        # check for frequency
        if data[item].get('frequency') != '':
                unit_IRI = '<http://www.ontology-of-units-of-measure.org/resource/om-2/Unit>'
                unit_string = str(data[item].get('frequency').get('unit'))
                unit_string=re.sub("°","deg",unit_string)
                unit_uuid = find_uuid('Unit' , unit_IRI, unit_string)

        # check for ionization mode
        if data[item].get('ionization_mode') != '':
                im_IRI = '<http://www.theworldavatar.com/ontology/ontospecies/OntoSpecies.owl#IonizationMode>'
                im_string = data[item].get('ionization_mode')
                im_uuid = find_uuid('IonizationMode' , im_IRI, im_string)

        # check for instrument type
        if data[item].get('instrument_type') != '':
                it_IRI = '<http://www.theworldavatar.com/ontology/ontospecies/OntoSpecies.owl#InstrumentType>'
                it_string = data[item].get('instrument_type')
                it_uuid = find_uuid('InstrumentType' , it_IRI, it_string)
        
        # check for instrument solvent
        if data[item].get('solvent') != '':
                solvent_IRI = '<http://www.theworldavatar.com/ontology/ontospecies/OntoSpecies.owl#Solvent>'
                solvent_string = data[item].get('solvent')
                solvent_uuid = find_uuid('Solvent' , solvent_IRI, solvent_string)

        if data[item].get('type')=='1DNMRSpectra':
            insert_str = pubchem_1DNMR_insert(uuid, i, prov_uuid, unit_uuid, solvent_uuid, it_uuid, data[item])
        elif data[item].get('type')=='2DNMRSpectra':
            insert_str = pubchem_2DNMR_insert(uuid, i, prov_uuid, unit_uuid, solvent_uuid, it_uuid, data[item])
        elif data[item].get('type') == 'MassSpectrometry':
            insert_str = pubchem_ms_insert(uuid, i, prov_uuid, im_uuid, it_uuid, data[item])

        prev_key = data[item].get('key')

        sparqlendpoint = SPARQL_ENDPOINTS['pubchem']
        # create a SPARQL object for performing the query
        kg_client = kg_operations(sparqlendpoint)
        kg_client.insertkg(insertStr=insert_str)


def find_uuid(name, typeIRI, string, comment = ''):
        IRI = get_uuid(typeIRI, string)
        if IRI:
            uuid = IRI.partition('_')[2]
        else:
            uuid = create_uuid()
            insert_str = generic_insert(name, typeIRI, uuid, string, comment)
            sparqlendpoint = SPARQL_ENDPOINTS['pubchem']
            # create a SPARQL object for performing the query
            kg_client = kg_operations(sparqlendpoint)
            kg_client.insertkg(insertStr=insert_str)
        return uuid
 
# create a new UUID
def create_uuid():
    return str(uuid.uuid4())