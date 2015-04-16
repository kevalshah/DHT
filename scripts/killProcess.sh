#!/bin/bash

# This script will attempt to terminate all server.jar files in the node list given if it is running.

if [ $# -eq 0 ]
    then
        echo Invalid number of arguments - Need to pass filename of hosts
	exit -1
fi

parallel-ssh -p 20 -h $1 -l ubc_eece411_4 -I -P < helperScripts/killProcess.sh