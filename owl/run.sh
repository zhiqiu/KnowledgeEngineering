#!/bin/sh
BIN="CreateOntology"
#cp ./bin/* .
java -cp bin -Djava.ext.dirs=./lib $BIN
