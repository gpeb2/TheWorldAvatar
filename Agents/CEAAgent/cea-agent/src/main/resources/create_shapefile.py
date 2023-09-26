"""
create_shapefile takes in the building geometry data retrieved from knowledge graph and
creates a shapefile in the input format required by CEA
"""

import shapely
import pandas as pd
import geopandas as gpd
import argparse
import json
from pyproj import CRS
import os

def create_shapefile(data, crs, shapefile):
    """
    :param geometries: Contains string of coordinates representing building envelope

    :param heights: Height above ground of building

    :param crs: coordinate reference system of the data

    :param shapefile: Name and path of shapefile to be created

    """

    heights = []
    floors_bg = []
    height_bg = []
    floor_height = []
    names = []
    floors_ag = []
    geometry = []

    ind = 0

    for building in data:
        geometries = data["geometry"]
        height = data["height"]
        id = data["id"]

        # Convert geometry data to arrays of points
        for geom in geometries:
            geometry.append(shapely.wkt.loads(geom))

            floors_bg.append(0)
            height_bg.append(0)
            floor_height.append(3.2)  # approximate floor-to-floor height

            if "zone.shp" in shapefile:
                initial = "B"
            else:
                initial = "S"

            if ind < 100:
                name = initial + str(ind).zfill(3)
            else:
                name = initial + str(ind)

            name += "_" + id # to identify geometries that belong to the same building

            names.append(name)

            ind += 1

            # calculate number of floors above ground
            floors = round(height / floor_height[-1])
            if floors != 0:
                floors_ag.append(floors)
            else:
                floors_ag.append(1)  # number of floors must be at least 1

            # CEA fails if height is less than or equal to 1 so set a minimum height of 1.00001 m
            if height <= 1.0:
                height = 1.00001

            heights.append(height)

    zone_data = {'Name': names,
                 'floors_bg': floors_bg,
                 'floors_ag': floors_ag,
                 'height_bg': height_bg,
                 'height_ag': heights}

    df = pd.DataFrame(data = zone_data)

    # crs is ESPG coordinate reference system id
    crs = CRS.from_user_input(int(crs))

    gdf = gpd.GeoDataFrame(df, crs = crs, geometry = geometry)
    gdf.to_file(shapefile, driver = 'ESRI Shapefile', encoding = 'ISO-8859-1')


def main(argv):
    shapefile_file = argv.file_name
    shapefile = argv.zone_file_location + os.sep + shapefile_file

    with open(argv.data_file_location, "r") as f:
        dataString = f.read()

    data_dictionary = json.loads(dataString)

    try:
        create_shapefile(data_dictionary, argv.crs, shapefile)
    except IOError:
        print('Error while processing file: ' + shapefile)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    # add arguments to the parser
    parser.add_argument("data_file_location")
    parser.add_argument("zone_file_location")
    parser.add_argument("crs")
    parser.add_argument("file_name")

    # parse the arguments
    args = parser.parse_args()
    main(args)
