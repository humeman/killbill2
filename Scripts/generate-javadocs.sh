#!/bin/bash

set -e

echo "-- Generating backend docs --"
if [ ! -d "Backend" ]; then
    echo "ERROR: Backend directory not found. You must be in the root project folder to run this command."
    exit 1
fi
cd Backend
./gradlew javadoc
cd ..
if [ -d "Documents/backend/javadoc" ]; then
    rm -r "Documents/backend/javadoc"
fi
mkdir -p Documents/backend/javadoc
cp -r Backend/backend/build/docs/javadoc/* Documents/backend/javadoc

echo "-- Generating frontend docs --"
if [ ! -d "Frontend" ]; then
    echo "ERROR: Frontend directory not found. You must be in the root project folder to run this command."
    exit 1
fi
cd Frontend
./gradlew javadoc
cd ..
if [ -d "Documents/frontend/javadoc/core" ]; then
    rm -r "Documents/frontend/javadoc/core"
fi
mkdir -p Documents/frontend/javadoc/
cp -r Frontend/core/build/docs/javadoc/* Documents/frontend/javadoc


git commit -m "[Auto] Update javadocs" --only Documents/backend/javadoc/ Documents/frontend/javadoc