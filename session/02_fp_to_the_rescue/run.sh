#!/bin/sh
if [ -f "$1" ]; then
	/usr/local/bin/scala -classpath "./lib/scala-async-0.97.jar" -feature -J-Xmx4g "${1}"
	rm *.class&>/dev/null
else
	echo "usage: ./run.sh <file.scala>"
	exit 0
fi

