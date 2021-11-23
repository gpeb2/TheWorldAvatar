from z_queries.queryKG import querykg
from z_queries.queryendpoint import SPARQL_ENDPOINTS
from z_queries.queryTemplates import getMOPIRIs
from z_queries.queryTemplates import mop_GBUs
from a_kg_overview.analytics_output import assemblyModel_json
from a_kg_overview.analytics_output import assemblyModel_json_ext
from a_kg_overview.analytics_output import assemblyModel_json_update
from a_kg_overview.analytics_output import r1_json
from a_kg_overview.analytics_output import preR2_json

def mopsoverview():
    """This Function runs preset query that returns back all of the MOP IRIs found in the OntoMOP KG"""
    result  = querykg(SPARQL_ENDPOINTS['ontomops'], getMOPIRIs()) #The query gets a full list of IRIs
    refinedlist = [] # Each IRI is saved in the list of refined IRIs
    for item in result:
        refined = item['mopIRI']
        refinedlist.append(refined)
    return refinedlist 

def assemblyModelGroups(listofMOPs):
    """Takes a list of MOP IRIs, queries the building untis, orders and gets formulas and counts assembly model."""
    uniques = {}
    mopFormulaList = []
    mopProvenance = []
    list_pregbus = []
    list_gbus = []
    list_R1 = [] # list of type [{AM_string:{GBU_information, GB2_information}}]
    for mopIRI in listofMOPs:     #### This loops over the MOPs list
        MOPandGBUs  = querykg(SPARQL_ENDPOINTS['ontomops'], mop_GBUs(mopIRI))
        i = 0
        savedMOPReferences = {} # Saved info so we do not need to requery again
        assemblyModel = {} # At this stage we create two parallel things 
        for MOPandGBU in MOPandGBUs: # for each queried MOP IRI we get two GBU lines as an output.
            assemblyModel['MOPFormula'] = MOPandGBU['MOPFormula']
            assemblyModel['mopIRI'] = MOPandGBU['mopIRI']
            assemblyModel['Symmetry'] = MOPandGBU['Symmetry'] # Ideally we should have the symmetry aslo as part of the MolFormRef
            savedMOPReferences['MOPReference'] = MOPandGBU['MOPReference'] # Saved so no need to requery
            savedMOPReferences['MOPFormula'] = MOPandGBU['MOPFormula'] # Saved so no need to requery
            i += 1
            gbu = {}
            if i == 1:
                cbuA = dict.copy(MOPandGBU)
            elif i == 2:
                cbuB = dict.copy(MOPandGBU)
        gbu = order(cbuA,cbuB) 
        assemblyModel.update(gbu)
        string = createAssemblyString(assemblyModel)                 
        print(assemblyModel['MOPFormula'], "_________________",  string)        
        gbu1 = str(gbu['CBU1_Modularity']+"-"+gbu['CBU1_Planarity'])
        gbu2 = str(gbu['CBU2_Modularity']+"-"+gbu['CBU2_Planarity'])
        gbu1dict = {gbu1:string}
        gbu2dict = {gbu2:string}
        if gbu1 not in list_pregbus:
            list_pregbus.append(gbu1)
        if gbu2 not in list_pregbus:
            list_pregbus.append(gbu2)
        if gbu1  in list_pregbus:
            pass        
        if gbu2 in list_pregbus:
            pass            
        if gbu1dict not in list_gbus:
            list_gbus.append(gbu1dict)
        if gbu2dict not in list_gbus:
            list_gbus.append(gbu2dict)
        if gbu1dict  in list_gbus:
            pass        
        if gbu2dict in list_gbus:
            pass            
        if savedMOPReferences['MOPReference'] not in mopFormulaList: 
            mopFormulaList.append(savedMOPReferences['MOPReference'])
            mopProvenance.append(savedMOPReferences)
        if string not in uniques.keys():
            uniques[str(string)] = 0
            frequency = uniques[str(string)]
            assemblyModel_json(assemblyModel, string)
            assemblyModel_json_ext(assemblyModel, string, frequency)
            list_R1.append({string:[str(gbu['CBU1_Modularity']+"-"+gbu['CBU1_Planarity']),str(gbu['CBU2_Modularity']+"-"+gbu['CBU2_Planarity'])]})
        if string in uniques.keys():
            uniques[str(string)] += 1/2
            frequency = uniques[str(string)] 
            assemblyModel_json_ext(assemblyModel, string, frequency)
            assemblyModel_json_update(string, frequency)
    print("UNIQUES\n")
    print(uniques)
    print("\n")
    print("LIST_R1\n")
    print(list_R1)
    r1_json(list_R1)
    print("\n")
    print("LIST_preR2\n")
    list_preR2 = mergeR2(list_pregbus, list_gbus)
    preR2_json(list_preR2) 
    print(list_preR2)
    return uniques

def mergeR2(list_pregbus, list_gbus):
    list_preR2 = {}
    for gbu_string in list_pregbus:
        list_ams = []
        for gbu_dict in list_gbus:
            if gbu_string in gbu_dict.keys():
                value = gbu_dict[gbu_string]
                list_ams.append(value)
            if gbu_string not in gbu_dict.keys():
                pass
        mydict = {gbu_string:list_ams}
        list_preR2.update(mydict)   
    return list_preR2    
        
def order(cbuA, cbuB):
    """This function takes two generic building units (GBUs), orders them and merges them into a single gbu dictionary"""
    mod_cbuA = int(cbuA['Modularity']) # The ordering is based on modularity. Alternatively it can be done on the nature of the cbu. i.e. organic vs inorganic. 
    mod_cbuB = int(cbuB['Modularity'])
    gbu = {}
    if mod_cbuA > mod_cbuB :
        gbu['CBU1'] = cbuA['CBUFormula']
        gbu['CBU1_Number'] = cbuA['NumberValue']
        gbu['CBU1_Modularity'] = cbuA['Modularity']
        gbu['CBU1_Planarity'] = cbuA['Planarity']
        gbu['CBU1_Type'] = cbuA['CBUType']
        gbu['CBU2'] = cbuB['CBUFormula']
        gbu['CBU2_Number'] = cbuB['NumberValue']
        gbu['CBU2_Modularity'] = cbuB['Modularity']
        gbu['CBU2_Planarity'] = cbuB['Planarity']
        gbu['CBU2_Type'] = cbuB['CBUType']
    else:
        gbu['CBU1'] = cbuB['CBUFormula']
        gbu['CBU1_Number'] = cbuB['NumberValue']
        gbu['CBU1_Modularity'] = cbuB['Modularity']
        gbu['CBU1_Planarity'] = cbuB['Planarity']
        gbu['CBU1_Type'] = cbuB['CBUType']
        gbu['CBU2'] = cbuA['CBUFormula']
        gbu['CBU2_Number'] = cbuA['NumberValue']
        gbu['CBU2_Modularity'] = cbuA['Modularity']
        gbu['CBU2_Planarity'] = cbuA['Planarity']
        gbu['CBU2_Type'] = cbuA['CBUType']
    return gbu

def createAssemblyString(assemblyModel):
    """The properties of an assembly model dictionary are transformed into single line string. The string is used for grouping."""
    ind_1 = assemblyModel['CBU1_Number']
    mod_1 = assemblyModel['CBU1_Modularity']
    pln_1 = assemblyModel['CBU1_Planarity']
    ind_2 = assemblyModel['CBU2_Number']
    mod_2 = assemblyModel['CBU2_Modularity']
    pln_2 = assemblyModel['CBU2_Planarity']
    symMOP = assemblyModel['Symmetry']
    assemblyStr = "("+ mod_1 + "-" + pln_1 + ")x" + ind_1 + "(" + mod_2 + "-" + pln_2 + ")x" + ind_2 + "___(" + symMOP + ")" 
    return assemblyStr

def createAssemblyString(assemblyModel):
    """The properties of an assembly model dictionary are transformed into single line string. The string is used for grouping."""
    ind_1 = assemblyModel['CBU1_Number']
    mod_1 = assemblyModel['CBU1_Modularity']
    pln_1 = assemblyModel['CBU1_Planarity']
    ind_2 = assemblyModel['CBU2_Number']
    mod_2 = assemblyModel['CBU2_Modularity']
    pln_2 = assemblyModel['CBU2_Planarity']
    symMOP = assemblyModel['Symmetry']
    assemblyStr = "("+ mod_1 + "-" + pln_1 + ")x" + ind_1 + "(" + mod_2 + "-" + pln_2 + ")x" + ind_2 + "___(" + symMOP + ")" 
    return assemblyStr