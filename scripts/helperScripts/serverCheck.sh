#!/bin/bash

# This is a helper script. It is not meant to be run independently.

# Color definitions
red='\033[0;31m'
green='\033[0;32m'
orange='\033[0;33m'
NC='\033[0m' # No Color

result=$(ps -A -o pid,cmd|grep "server-group9.jar" | grep -v grep |head -n 1)

if [ ${#result} -eq 0 ]; then
    echo -e "${red}FAIL server.jar is not running${NC}"
else
    procid=$(ps -A -o pid,cmd|grep "server-group9.jar" | grep -v grep |head -n 1 | awk '{print $1}')
    port=$(sudo netstat -tulp | grep $procid | awk '{print $4}')
    echo -e "${green}SUCCESS $result ${orange}PORT:$port${NC}"
fi
