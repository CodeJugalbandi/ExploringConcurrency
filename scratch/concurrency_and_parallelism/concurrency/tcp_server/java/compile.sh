if [ -f "$1" ]; then
	echo "Cleaning class files..."
	rm -v *.class
	echo "Compiling ${1}..."
	FILENAME=$(echo "$1" | rev | cut -f 1 -d '/' | rev)
	javac -classpath .:./lib/* "${FILENAME}"
	CLASSNAME=$(echo "$FILENAME" | cut -f 1 -d '.')
  echo "DONE"
else
	echo "usage: ./compile.sh <file.java>"
	exit 0
fi
