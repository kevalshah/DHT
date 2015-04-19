#!/bin/bash

# This script will attempt to start the server.jar on the input node list if it exists there.

# Color definitions
red='\033[0;31m'
green='\033[0;32m'
orange='\033[0;33m'
NC='\033[0m' # No Color


if [ $# -eq 0 ]
    then
        echo Invalid number of arguments - Need to pass filename of hosts
	exit -1
fi

ID=1
NODEID=3
while read line
do
	host=$line

	if ( ssh -n -f ubc_eece411_4@$host '[ -d /DHT-Group9 ]' )
	then
        result="`ssh -n -f ubc_eece411_4@$host "ps -A -o pid,cmd|grep '$3' | grep -v grep |head -n 1"`"
        if [ ${#result} -eq 0 ]; then
            ssh -n -f ubc_eece411_4@$host "sh -c 'java -Xmx64m -jar DHT-Group9/server-group9.jar -n $NODEID -c $2 -p $3 > /dev/null 2>&1 &'"            check="`ssh -n -f ubc_eece411_4@$host "ps -A -o pid,cmd|grep '$3' | grep -v grep |head -n 1"`"
            check="`ssh -n -f ubc_eece411_4@$host "ps -A -o pid,cmd|grep '$3' | grep -v grep |head -n 1"`"
            if [ ${#check} -eq 0 ]; then
                echo -e "${orange}[$ID] $host${NC} - ${red}[FAILURE] server.jar could not start${NC}"
            else
                echo -e "${orange}[$ID] $host${NC} - ${green}[SUCCESS] Process started ${orange}NODE ID:$NODEID${NC}"
                let NODEID=NODEID+3
            fi
        else
            echo -e "${orange}[$ID] $host${NC} - ${red}[FAILURE] server.jar is already running${NC}"
        fi
    else
        echo -e "${orange}[$ID] $host${NC} - ${red}[FAILURE] server.jar does not exist${NC}"
    fi

	let ID=ID+1

done < $1