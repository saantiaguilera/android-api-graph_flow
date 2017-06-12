#!/bin/bash

./gradlew shouldPublish

if [ $? -eq 0 ]; then
	for module in ${PUBLISH_MODULES[@]}; do
		./gradlew ${module}:bintrayUpload

		if [ $? -ne 0 ]; then
			exit 1
		fi
    done
fi

exit 0