from a_kg_overview.local_manager import kgoverview
from b_lib1_creation.local_manager import lib1_Creation
def workflow():
    output_kgoverview = kgoverview() # the output of kg overview is [0] List_AM_GBU and [1] List_preR2
    lib1_Creation(output_kgoverview) # this function is running and creating the first batch of files
    return output_kgoverview
    #list_AM_GBU = output_kgoverview[0]
    #list_preR2 =  output_kgoverview[1]
    #lib1_Creation(list_AM_GBU) # this function is running and creating the first batch of files
    #lib1_Update(list_AM_GBU) # this function is updating the cbu files
    #list_R2 = cbu_Overlap(list_preR2) # this function is checking if there are shared gbus
    #lib1_Creation(list_R2)
    #assembler(list_AM_GBU)
    

