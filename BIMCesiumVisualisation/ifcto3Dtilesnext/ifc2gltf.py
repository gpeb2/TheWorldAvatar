"""
# Author: qhouyee #

This module generates glTF models from the IFC file.
"""

# Standard library imports
import subprocess

# Third party imports
from ifcopenshell.util.selector import Selector

# Self imports
from utils import find_word


def verify_feature_exists(featurelist, ifc):
    """
    Verifies if the IFC feature in a list exist in the IFC file

    Arguments:
        featurelist - A list of IfcClasses to verify in string. Accepted format: '.IfcFeatureType'
        ifc - The required IFC model loaded in ifcopenshell
    Returns:
    A list containing features that exist in the format 'IfcFeatureType'
    """
    # selector is a utility tool from  ifcopenshell
    selector = Selector()
    result_list = []
    for feature in featurelist:
        if len(selector.parse(ifc, feature)) > 0:
            result_list += [feature[1:]]
    return result_list


def gendict4split(ifc):
    """
    Creates a dictionary {filename : ifc_classes or ifc_id} to split
    the IFC model into smaller IFC files

    Arguments:
        ifc - The required IFC model loaded in ifcopenshell
    Returns:
        The required dictionary for splitting the IFC model
        A hashtable to match assets to their IFC ID
    """
    # Initialise a dictionary with the IFC classes to exclude from the building output model
    # Non-exhaustive. If required, add more classes in the format 'IfcFeatureType'
    dict_elements = {"building":
                     ["IfcBuildingElementProxy", "IfcFurnishingElement",
                      "IfcFlowTerminal", "IfcSpace", "IfcOpeningElement"]}

    hashmapping = {}
    counter = 1
    # Store the IDs that should be generated as part of the interior furniture or solar panel model
    furniture_elements = []
    solar_panel_elements = []
    for feature in ["IfcBuildingElementProxy", "IfcFurnishingElement", "IfcFlowTerminal"]:
        for element in ifc.by_type(feature):
            # If the name contains these key words,generate individual models for them
            wordlist = ["Sensor", "Weather Station", "Fridge", "Meter"]
            if find_word(wordlist, element.Name):
                # Simplify the name of asset files, as
                # Cesium cannot load complicated or long file names
                dict_elements["asset"+str(counter)] = element.GlobalId
                # Create a hashtable dict to store the mapping values
                hashmapping[element.GlobalId] = {
                    "file": "asset"+str(counter), "name": element.Name}
                counter = counter+1
            # If the name contain a solar panel, generate a separate solar panel model
            elif find_word(["Solar Panel"], element.Name):
                solar_panel_elements.append(element.GlobalId)
            else:
                furniture_elements.append(element.GlobalId)

    # Add to dictionary if list is not empty
    if furniture_elements:
        dict_elements["furniture"] = furniture_elements
    if solar_panel_elements:
        dict_elements["solarpanel"] = solar_panel_elements
    return dict_elements, hashmapping


def append_ifcconvert_command(key, value, ifcconvert_command):
    """
    Appends the dictionary values to the commands depending on
    their key and if they are either a list or single instance

    Arguments:
        key - The dictionary key indicating the model output name
        value - The dictionary values indicating either the IFC classes
                or ID for inclusion or exclusion
        ifcconvert_command - The initialised command in List format
    Returns:
    The command with the appended values
    """
    # Add the entities arg for only building key
    if key == "building":
        ifcconvert_command += ["--exclude", "entities"]
        id_attributes = []
        while len(value) > 0:
            item = value.pop(0)
            if isinstance(item, dict):
                id_attributes = item["id"]
            else:
                ifcconvert_command += [item]
        # If there are IDs available, append them to the command
        if id_attributes:
            ifcconvert_command += ["attribute", "GlobalId"]
            ifcconvert_command += id_attributes
    # Other keys will only require an attribute arg
    else:
        ifcconvert_command += ["--include", "attribute", "GlobalId"]
        # Value may be either a list or a single object
        if isinstance(value, list):
            ifcconvert_command += value
        else:
            ifcconvert_command += [value]
    return ifcconvert_command


def conv2gltf(ifc, input_ifc):
    """
    Invokes the related external tools to split an IFC file and
    convert to the glTF format in the gltf subfolder

    Arguments:
        ifc - The required IFC model loaded in ifcopenshell
        input_ifc - Local file path of ifc model
    Returns:
        A hashtable linking each IFC id with their glTF file name
    """
    dict_for_split, hashmapping = gendict4split(ifc)

    for key, value_list in dict_for_split.items():
        daepath = "./data/dae/" + key + ".dae"
        gltfpath = "./data/gltf/" + key + ".gltf"
        # Initialise the command and append accordingly
        ifcconvert_command = ["./resources/IfcConvert", input_ifc, daepath]
        ifcconvert_command = append_ifcconvert_command(
            key, value_list, ifcconvert_command)
        # Convert from IFC to DAE to glTF
        subprocess.run(ifcconvert_command, check=True)
        subprocess.run(
            ["./resources/COLLADA2GLTF/COLLADA2GLTF-bin",
                "-i", daepath, "-o", gltfpath],
            check=True)
    print("Conversion to gltf completed...")
    return hashmapping
