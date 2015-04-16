#!/bin/bash

# This is a helper script. It is not meant to be run independently.

# Color definitions
red='\033[0;31m'
green='\033[0;32m'
NC='\033[0m' # No Color

JAVA_VER=$(java -version 2>&1)
substring="Java(TM)"
if [ "${JAVA_VER/$substring}" = "$JAVA_VER" ] ; then
  echo -e "${red}FAIL Java is not installed${NC}"
else
  echo -e "${green}SUCCESS Java is installed${NC}"
fi
