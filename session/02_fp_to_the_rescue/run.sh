#!/bin/bash
LOCAL_CLASSPATH="$HOME/lib/scala-async-0.97.jar"

if [ -f "$1" ]; then
	/usr/local/bin/scala -classpath $LOCAL_CLASSPATH "${1}"
	rm *.class&>/dev/null
else
	/usr/local/bin/scala -classpath $LOCAL_CLASSPATH
	echo "usage: ./run.sh <file.scala>"
	exit 0
fi

