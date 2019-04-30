#!/bin/sh
echo "Cleaning class files..."
rm -rfv ./tsys
scalac -deprecation *.scala
echo "Running..."
scala tsys.stocks.App
