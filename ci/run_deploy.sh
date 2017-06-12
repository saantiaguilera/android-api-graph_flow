#!/bin/bash

# Modules that will be published
PUBLISH_MODULES=(core conductor views fragments)

./gradlew shouldPublish | sed -f "$STDOUT_FILTERS"

EXIT_CODE=${PIPESTATUS[0]}

if [ $EXIT_CODE -eq 0 ]; then
	VERSION=$(grep "versionName = .*" gradle.properties | sed -E "s/versionName = (.*)/\1/g")
	git tag -a v${VERSION} -m "v${VERSION}"

	for module in ${PUBLISH_MODULES[@]}; do
		./gradlew ${module}:bintrayUpload

		if [ $? -ne 0 ]; then
			exit 1
		fi
    done
fi

exit 0