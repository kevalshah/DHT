#!/bin/bash

ID=10000
COUNTER=0
PORT=51613

# Start contact node
java -jar ../out/artifacts/server/server-group9.jar $1

#while [  $COUNTER -lt 3 ]; do
#
#    java -jar ../out/artifacts/server/server-group9.jar -c localhost:51612 -p $PORT -n $ID &
#
#    let ID=ID+1000
#    let COUNTER=COUNTER+1
#    let PORT=PORT+1
#done