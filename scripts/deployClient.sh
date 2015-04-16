#!/bin/bash

if [ $# -eq 0 ]
    then
        echo Invalid number of arguments - Need to pass filename of hosts
	exit -1
fi

parallel-rsync -p 20 -h $1 -l ubc_eece411_4 ../out/artifacts/client/client.jar /home/ubc_eece411_4/DHT-Group9/