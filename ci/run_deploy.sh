#!/bin/bash

./gradlew shouldPublish | sed -f "$STDOUT_FILTERS"

EXIT_CODE=${PIPESTATUS[0]}

if [ $EXIT_CODE -eq 0 ]; then
	for module in ${PUBLISH_MODULES[@]}; do
		./gradlew ${module}:bintrayUpload

		if [ $? -ne 0 ]; then
			exit 1
		fi
    done
fi

exit 0