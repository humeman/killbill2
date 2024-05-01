#!/bin/bash

set -e

usage() {
    echo "Usage: ./open-javadocs.sh frontend OR ./open-javadocs.sh backend"
    exit 1
}

if [ ! -n "$1" ]; then
    usage
fi

if [ "$1" == "frontend" ] || [ "$1" == "backend" ]; then
    sensible-browser "Documents/$1/javadoc/index.html"
else
    usage
fi