# template has one slot: species
ROTATIONAL_CONSTANT_QUERY = ''' 
PREFIX compchemkb: <https://como.cheng.cam.ac.uk/kb/compchem.owl#>
PREFIX gc: <http://purl.org/gc/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX ontocompchem:<http://www.theworldavatar.com/ontology/ontocompchem/ontocompchem.owl#>

SELECT DISTINCT  ?name ?rotational_constants_value ?unit_short
WHERE  { 
?g_calculation rdf:type ontocompchem:G09 .
?g_calculation ontocompchem:hasInitialization ?initialization .
?initialization gc:hasMoleculeProperty ?molecule_property .
?molecule_property gc:hasName ?name .
FILTER regex(?name, "^%s $")
# ============ to match molecule =========================
?g_calculation gc:isCalculationOn ?rotational_constants .
?rotational_constants ontocompchem:hasRotationalConstants ?rotational_constants_value . 
OPTIONAL {
?rotational_constants gc:hasUnit ?unit .
BIND(REPLACE(STR(?unit),"http://data.nasa.gov/qudt/owl/unit#","") AS ?unit_short) .
}
} 
'''

VIBRATION_FREQUENCY_QUERY = '''
PREFIX compchemkb: <https://como.cheng.cam.ac.uk/kb/compchem.owl#>
PREFIX gc: <http://purl.org/gc/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX ontocompchem:<http://www.theworldavatar.com/ontology/ontocompchem/ontocompchem.owl#>
SELECT DISTINCT   ?frequency ?name ?unit_short
WHERE  { 
?g_calculation rdf:type ontocompchem:G09 .
?g_calculation ontocompchem:hasInitialization ?initialization .
?initialization gc:hasMoleculeProperty ?molecule_property .
?molecule_property gc:hasName ?name .
FILTER regex(?name, "^%s $")

# ============ to match molecule =========================
?g_calculation  gc:isCalculationOn  ?VibrationalAnalysis .
?VibrationalAnalysis rdf:type gc:VibrationalAnalysis .
?VibrationalAnalysis gc:hasResult ?result . 
?result ontocompchem:hasFrequencies ?frequency .
OPTIONAL {
?result gc:hasUnit ?unit .
BIND(REPLACE(STR(?unit),"http://purl.org/gc/","") AS ?unit_short) .
}
}   
'''

ROTATIONAL_SYMMETRY_NUMBER = '''
PREFIX compchemkb: <https://como.cheng.cam.ac.uk/kb/compchem.owl#>
PREFIX gc: <http://purl.org/gc/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX ontocompchem:<http://www.theworldavatar.com/ontology/ontocompchem/ontocompchem.owl#>
SELECT DISTINCT  ?name   ?symmetry_number  
WHERE  { 
?g_calculation rdf:type ontocompchem:G09 .
?g_calculation ontocompchem:hasInitialization ?initialization .
?initialization gc:hasMoleculeProperty ?molecule_property .
?molecule_property gc:hasName ?name .
FILTER regex(?name, "^%s $")
# ============ to match molecule =========================
?g_calculation  gc:isCalculationOn  ?RotationalSymmetry .
?RotationalSymmetry rdf:type ontocompchem:RotationalSymmetry .
?RotationalSymmetry ontocompchem:hasRotationalSymmetryNumber ?symmetry_number .
}   
'''

GAUSSIAN_FILE = '''
PREFIX compchemkb: <https://como.cheng.cam.ac.uk/kb/compchem.owl#>
PREFIX gc: <http://purl.org/gc/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX ontocompchem:<http://www.theworldavatar.com/ontology/ontocompchem/ontocompchem.owl#>
SELECT DISTINCT  ?name   ?File
WHERE  {
?g_calculation rdf:type ontocompchem:G09 .
?g_calculation ontocompchem:hasInitialization ?initialization .
?initialization gc:hasMoleculeProperty ?molecule_property .
?molecule_property gc:hasName ?name .
FILTER regex(?name, "^%s $")
# ============ to match molecule =========================
?g_calculation  ontocompchem:hasEnvironment   ?Environment .
?Environment    gc:hasOutputFile  ?File . 
}
'''