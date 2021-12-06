from a_kg_overview.local_manager import kgoverview
from b_lib1_creation.assembly_lib import lib1_Creation
from d_cbu_overlap.cbuoverlap import cbuoverlap
from e_lib2_creation.lib2_creation import lib2_creation
from e_lib2_creation.missing_files import add_missing_files
from f_assembler.assembler import searchRadius

def workflow():
    """ """
    output_kgoverview = kgoverview() 
    
    lib1_Creation(output_kgoverview[0]) # this function is running and creating the first batch of files
    cbuoverlap(output_kgoverview[1])
    lib2_creation(output_kgoverview[1])
    add_missing_files(output_kgoverview[0])
    search_analytics = searchRadius(output_kgoverview[0], output_kgoverview[2])
    return search_analytics
    #list_AM_GBU = output_kgoverview[0]
    #list_preR2 =  output_kgoverview[1]
    #lib1_Creation(list_AM_GBU) # this function is running and creating the first batch of files
    #lib1_Update(list_AM_GBU) # this function is updating the cbu files
    #list_R2 = cbu_Overlap(list_preR2) # this function is checking if there are shared gbus
    #lib1_Creation(list_R2)
    #assembler(list_AM_GBU)
    

