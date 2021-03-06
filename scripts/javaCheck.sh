#!/bin/bash

# This script will check the java version on the input node list

if [ $# -eq 0 ]
    then
        echo Invalid number of arguments - Need to pass filename of hosts
	exit -1
fi

parallel-ssh -h $1 -l ubc_eece411_4 -I -P < helperScripts/javaVersionCheck.sh