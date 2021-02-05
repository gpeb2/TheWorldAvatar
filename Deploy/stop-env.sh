#!/bin/sh

# Wrapper script for docker-compose that brings down the stack for the requested environment-mode combination.
#
# Run this script with no arguments for usage instructions.


# Bail out if the environment name and mode weren't supplied
if [ "$#" -ne 2 ]; then
  echo "============================================================================="
  echo " Usage:"
  echo "  $0 [env_name] [mode]"
  echo ""
  echo "  [env_name] : the environment to stop (agent/db/web)"
  echo "      [mode] : configuration mode name (dev/test/prod)"
  echo "============================================================================="
  exit 1
fi

# Read env and mode from the first two args
env=$1
mode=$2

# Check that a valid env name was supplied
case $env in
  db) ;;
  agent) ;;
  web) ;;
  *)
    echo "[$env] is not a recognised environment name; choose 'agent', 'db', or 'web'."
    exit 2
esac

# Check that a valid mode was supplied and set corresponding docker-compose files
case $mode in
  dev)
    ;;
  test)
    ;;
  prod)
    ;;
  *)
    echo "[$mode] is not a recognised mode - choose 'dev', 'test' or 'prod'."
    exit 3
    ;;
esac
compose_files="docker-compose.yml docker-compose.$mode.yml"
compose_file_args=$(echo $compose_files |sed -e 's/ / -f /' -e 's/^/-f /')

printf "Stopping the $mode-$env environment\n\n"

# Run docker-compose (switch to environment dir to simplify finding config files)
pushd $env > /dev/null
docker_compose_cmd="docker-compose $compose_file_args -p $mode-$env down"
$docker_compose_cmd
compose_exit_code=$?
popd > /dev/null

if [ $compose_exit_code -eq 0 ]; then
  printf "\nDone\n"
else
  printf "\n'docker-compose down' failed with exit code $compose_exit_code\n"
fi
