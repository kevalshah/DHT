#!/bin/bash

if [ $# -ne 3 ]
    then
        echo Need to pass in 3 arguments: file with hosts, output file to write to, port to append in that respective order
	exit -1
fi

var=$3
awk -v v1=$var '{print $0":"v1}' $1 > $2

