#!/bin/sh
if [ -f "$1" ]; then
    echo "Running ${1}..."
	scala -classpath .:./lib/* "${1}"
	echo "*** Run Complete ***"
else
	echo "usage: ./run.sh <file.scala>"	
	exit 0
fi