#!/bin/sh  
# Define some constants  
#OWL=owl
#PROJECT_PATH= $PWD
JAR_PATH=$PROJECT_PATH/lib  
BIN_PATH=$PROJECT_PATH/bin  
SRC_PATH=$PROJECT_PATH/src
  
# First remove the sources.list file if it exists and then create the sources file of the project  
#rm -f $SRC_PATH/sources  
#find $SRC_PATH -name *.java > $SRC_PATH/sources.list  
  
  
# Compile the project  
#javac -Djava.ext.dirs=./lib @$SRC_PATH/sources.list  
javac -d ./bin -Djava.ext.dirs=./lib ./src/*.java
