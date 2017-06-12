#!/bin/bash

STDOUT_FILTERS="travis/stdout_filters.sed"

./gradlew -Dorg.gradle.daemon=true $TEST_SUITE -PdisablePreDex | sed -f "$STDOUT_FILTERS"

EXIT_CODE=${PIPESTATUS[0]}

if [ $EXIT_CODE -ne 0 ]; then
    exit $EXIT_CODE
fi