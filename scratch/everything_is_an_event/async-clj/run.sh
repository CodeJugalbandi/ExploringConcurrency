#!/usr/bin/env bash

if [[ ! -x `which clj` ]]; then
    echo "Please install clj command-line script to run Clojure files." && exit 1
fi

echo "Starting. Use ^C to stop."
clj -m stock-ticker
