#!/bin/bash

#
# This script copies the committed configuration files and the data provided by the 
# user to the correct places before starting the stack, then uploading the data.
# 
# Author: Michael Hillman (mdhillman<@>cmcl.io)
#

# Parse arguments
for ARGUMENT in "$@"
do
   KEY=$(echo $ARGUMENT | cut -f1 -d=)
   KEY_LENGTH=${#KEY}
   VALUE="${ARGUMENT:$KEY_LENGTH+1}"
   export "$KEY"="$VALUE"
done

# Check for required arguments
if [[ ! -v PASSWORD ]]; then 
    echo "No PASSWORD argument supplied, cannot continue."
    exit -1
fi

# Store starting directory
START=$(pwd)
echo "$START"

echo 
echo "This script will remove any existing stack manager configurations, launch a new"
echo "stack for the UK Base World, then upload the supplied data sets and dashboards."
echo 
read -p "Has the data been provided and required packages installed (Y/N)? " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]
then
    
    # Get the root of the current Git repo
    ROOT=$(git rev-parse --show-toplevel)
    if [[ "$ROOT" == *"not a git"* ]]; then
        echo "Not within a valid Git repository, cannot continue."
        exit -1
    fi

    # Copy in the FIA config and query
    FIA_DIR="$ROOT/Agents/FeatureInfoAgent/queries"
    cp "./inputs/config/fia-config.json" "$FIA_DIR/"
    cp "./inputs/config/dukes_query.sparql" "$FIA_DIR/"

    # Clear any existing stack manager configs
    MANAGER_CONFIG="$ROOT/Deploy/stacks/dynamic/stack-manager/inputs/config/"
    rm -rf "$MANAGER_CONFIG/*.json"

    # Write out the stack passwords
    echo $PASSWORD > "$ROOT/Deploy/stacks/dynamic/stack-manager/inputs/secrets/geoserver_password"
    echo $PASSWORD > "$ROOT/Deploy/stacks/dynamic/stack-manager/inputs/secrets/postgis_password"
    echo $PASSWORD > "$ROOT/Deploy/stacks/dynamic/stack-manager/inputs/secrets/grafana_password"

    # Copy in the stack manager configs
    cp "./inputs/config/UKBASEWORLD.json" "$MANAGER_CONFIG/"
    cp "./inputs/config/visualisation.json" "$MANAGER_CONFIG/services/"
    cp "./inputs/config/grafana.json" "$MANAGER_CONFIG/services/"

    # Copy the FIA files into the special volume populator folder
    mkdir -p "$ROOT/Deploy/stacks/dynamic/stack-manager/inputs/data/fia-queries"
    rm -rf "$ROOT/Deploy/stacks/dynamic/stack-manager/inputs/data/fia-queries/*"
    cp "./inputs/config/fia-config.json" "$ROOT/Deploy/stacks/dynamic/stack-manager/inputs/data/fia-queries/"
    cp "./inputs/config/dukes_query.sparql" "$ROOT/Deploy/stacks/dynamic/stack-manager/inputs/data/fia-queries/"

    # Copy the visualisation files into the special volume populator folder
    mkdir -p "$ROOT/Deploy/stacks/dynamic/stack-manager/inputs/data/vis-files/"
    rm -rf "$ROOT/Deploy/stacks/dynamic/stack-manager/inputs/data/vis-files/*"
    cp -r "./visualisation/webspace/." "$ROOT/Deploy/stacks/dynamic/stack-manager/inputs/data/vis-files/"
   
    # Copy in the custom grafana conf into the special volume populator folder
    mkdir -p "$ROOT/Deploy/stacks/dynamic/stack-manager/inputs/data/grafana-conf/"
    rm -rf "$ROOT/Deploy/stacks/dynamic/stack-manager/inputs/data/grafana-conf/*"
    cp -r "./inputs/dashboard/grafana-config.ini" "$ROOT/Deploy/stacks/dynamic/stack-manager/inputs/data/grafana-conf/"

    # Copy in the mapbox secret files
    cp "./visualisation/mapbox_api_key" "$ROOT/Deploy/stacks/dynamic/stack-manager/inputs/secrets/"
    cp "./visualisation/mapbox_username" "$ROOT/Deploy/stacks/dynamic/stack-manager/inputs/secrets/"

    # Clear any existing stack uploader configs
    UPLOAD_CONFIG="$ROOT/Deploy/stacks/dynamic/stack-data-uploader/inputs/config"
    rm -rf "$UPLOAD_CONFIG/*.json"

    # Copy in the stack uploader config(s)
    cp "./inputs/config/dukes_2023.json" "$UPLOAD_CONFIG/"

    # Copy in the data for upload
    UPLOAD_DATA="$ROOT/Deploy/stacks/dynamic/stack-data-uploader/inputs/data"
    cp -r "./inputs/data/." "$UPLOAD_DATA/" 

    # Run the stack manager to start a new stack
    cd "$ROOT/Deploy/stacks/dynamic/stack-manager"
    echo "Running the stack start up script..."
    ./stack.sh start UKBASEWORLD

    # Wait for the stack to start
    echo 
    read -p "Press enter to continue once the stack is up and running..."
    
    # Run the uploader to upload data
    cd "$ROOT/Deploy/stacks/dynamic/stack-data-uploader"
    echo "Running the stack uploader script..."
    ./stack.sh start UKBASEWORLD 38383

    # Get the name of the grafana container
    GRAFANA=$(docker container ls | grep -o UKBASEWORLD-grafana.*)

    # Copy in the custom grafana images
    cd "$START"
    docker cp "./inputs/twa-logo.svg" $GRAFANA:"/usr/share/grafana/public/img/grafana_icon.svg"
    docker cp "./inputs/twa-favicon.png" $GRAFANA:"/usr/share/grafana/public/img/fav32.png"
    docker cp "./inputs/twa-favicon.png" $GRAFANA:"/usr/share/grafana/public/img/apple-touch-icon.png"
    docker cp "./inputs/twa-background-light.svg" $GRAFANA:"/usr/share/grafana/public/img/g8_login_light.svg"
    docker cp "./inputs/twa-background-dark.svg" $GRAFANA:"/usr/share/grafana/public/img/g8_login_dark.svg"

    # Create a new Grafana organisation via HTTP API
    echo "Using Grafana API to create a new organisation..."
    RESPONSE="$(curl -X POST -H "Content-Type: application/json" -d '{"name":"CMCL"}' http://admin:$PASSWORD@localhost:38383/dashboard/api/orgs)"
    ORG_ID="$(echo $RESPONSE | grep -o [0-9])"

    # Change the Grafana admin account to be part of the new org
    echo "Using Grafana API to switch admin account to the new organisation..."
    curl -X POST http://admin:$PASSWORD@localhost:38383/dashboard/api/user/using/$ORG_ID

    # Create an API token
    echo "Using Grafana API to create an API token..."
    RESPONSE="$(curl -X POST -H "Content-Type: application/json" -d '{"name":"apikeycurl", "role": "Admin"}' http://admin:$PASSWORD@localhost:38383/dashboard/api/auth/keys)"
    API_KEY="$(jq '.key' <<< $RESPONSE)"
    API_KEY="$( echo "$API_KEY" | tr -d '"')"

    echo "API KEY IS $API_KEY"
    echo $API_KEY > ./grafana_api_key
    echo "Newly created Grafana API key has been written to the 'grafana_api_key' file."

    # Create a new grafana data source
    echo "Using Grafana API to add a connection to the PostGIS database..."
    sed "s|\"PASSHERE\"|\"$PASSWORD\"|g" ./inputs/dashboard/datasource.json > ./inputs/dashboard/datasource.temp

    RESPONSE="$(curl -X POST -H "Content-Type: application/json" -H "Authorization: Bearer $API_KEY" --data-binary @inputs/dashboard/datasource.temp http://admin:$PASSWORD@localhost:38383/dashboard/api/datasources)"
    echo "$RESPONSE"
    SOURCE_UID="$(jq '.datasource.uid' <<< $RESPONSE)"

    # Upload grafana dashboards via HTTP API
    echo "Uploading saved Grafana dashboards..."
    sed "s|\"SOURCEUID\"|$SOURCE_UID|g" ./inputs/dashboard/dashboard-raw-data.json > ./inputs/dashboard/dashboard-raw-data.temp
    sed "s|\"SOURCEUID\"|$SOURCE_UID|g" ./inputs/dashboard/dashboard-overview.json > ./inputs/dashboard/dashboard-overview.temp
    curl -X POST --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $API_KEY" --data-binary @inputs/dashboard/dashboard-raw-data.temp http://admin:$PASSWORD@localhost:38383/dashboard/api/dashboards/db
    curl -X POST --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $API_KEY" --data-binary @inputs/dashboard/dashboard-overview.temp http://admin:$PASSWORD@localhost:38383/dashboard/api/dashboards/db

    # Update the default grafana dashboard to be the "Overview" one
    curl -X PUT --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $API_KEY" --data-binary @inputs/dashboard/grafana-org-config.json http://admin:$PASSWORD@localhost:38383/dashboard/api/org/preferences

    echo 
    echo "Script completed, may need to wait a few minutes for the stack-data-uploader to finish."
    echo "Visualisation should be available now at http://localhost:38383/visualisation/"
else
    echo "Please copy in the data sets as described in the README before re-running this script."
    exit -1
fi