#!/bin/bash

# This is a helper script. It is not meant to be run independently.

# Color definitions
red='\033[0;31m'
green='\033[0;32m'
NC='\033[0m' # No Color

result=$(ps -A -o pid,cmd|grep "DHT-Group9/server-group9.jar" | grep -v grep |head -n 1)

if [ ${#result} -eq 0 ]; then
    echo -e "${red}FAIL server-group9.jar is not running${NC}"
else
    procid=$(ps -A -o pid,cmd|grep "DHT-Group9/server-group9.jar" | grep -v grep |head -n 1 | awk '{print $1}')
    if [ ${#procid} -eq 0 ]; then
        echo -e "${red}FAIL Process not killed: $result${NC}"
    else
        kill $procid
        echo -e "${green}SUCCESS PID $procid killed: $result${NC}"
    fi
fi

