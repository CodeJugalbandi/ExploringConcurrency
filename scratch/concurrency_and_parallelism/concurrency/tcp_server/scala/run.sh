#!/bin/sh

if [ -f "$1" ]; then
  echo "Cleaning class files..."
  rm -rfv com
  echo "Compiling scala files..."
  FILENAME=$(echo "$1" | rev | cut -f 1 -d '/' | rev)
  scalac -deprecation *.scala
  CLASSNAME=$(echo "$FILENAME" | cut -f 1 -d '.')
  echo "Running ${CLASSNAME}"
  scala "com.tsys.${CLASSNAME}"
else
  echo "usage: ./run.sh <file-containing-main.scala>"
  exit 0
fi
