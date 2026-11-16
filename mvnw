#!/bin/sh
set -e

MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(cd "$(dirname "$0")" && pwd)}"
MAVEN_WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"

if [ -f "$MAVEN_WRAPPER_PROPERTIES" ]; then
    distributionUrl=$(grep 'distributionUrl' "$MAVEN_WRAPPER_PROPERTIES" | cut -d'=' -f2-)
fi

MAVEN_DIST_DIR="$HOME/.m2/wrapper/dists"

if [ ! -d "$MAVEN_DIST_DIR/apache-maven-3.9.6" ]; then
    echo "Downloading Maven from $distributionUrl..."
    mkdir -p "$MAVEN_DIST_DIR"
    TMPFILE=$(mktemp)
    curl -fsSL -o "$TMPFILE" "$distributionUrl"
    unzip -q -o "$TMPFILE" -d "$MAVEN_DIST_DIR"
    rm -f "$TMPFILE"
fi

exec "$MAVEN_DIST_DIR/apache-maven-3.9.6/bin/mvn" "$@"
