#!/bin/bash

# This is a helper script. It is not meant to be run independently.

# Color definitions
red='\033[0;31m'
green='\033[0;32m'
orange='\033[0;33m'
NC='\033[0m' # No Color

if [ -d /home/ubc_eece411_4/DHT-Group9 ]; then
    result=$(ps -A -o pid,cmd|grep "server-group9.jar" | grep -v grep |head -n 1)
    if [ ${#result} -eq 0 ]; then
        java -Xmx64m -jar DHT-Group9/server-group9.jar -c planetlab4.williams.edu:51600 -p 51600 > /dev/null 2>&1 &
        check=$(ps -A -o pid,cmd|grep "server-group9.jar" | grep -v grep |head -n 1)
        if [ ${#check} -eq 0 ]; then
            echo -e "${red}FAIL server-group9.jar could not start${NC}"
        else
            procid=$(ps -A -o pid,cmd|grep "server-group9.jar" | grep -v grep |head -n 1 | awk '{print $1}')
            port=$(sudo netstat -tulp | grep $procid | awk '{print $4}')
            echo -e "${green}SUCCESS $check ${orange}PORT:$port${NC}"
        fi
    else
        echo -e "${red}FAIL server-group9.jar is already running${NC}"
    fi

else
    echo -e "${red}FAIL server-group9.jar does not exist${NC}"
fi