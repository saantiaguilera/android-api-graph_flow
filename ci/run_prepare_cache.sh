#!/bin/bash

./gradlew dependencies | sed -f "$STDOUT_FILTERS"

EXIT_CODE=${PIPESTATUS[0]}

if [ $EXIT_CODE -ne 0 ]; then
	exit $EXIT_CODE
fi