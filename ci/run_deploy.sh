#!/bin/bash

# Modules that will be published
PUBLISH_MODULES=(core conductor views fragments)

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

VERSION=$(grep "versionName = .*" gradle.properties | sed -E "s/versionName = (.*)/\1/g")
API_JSON=$(printf '{"tag_name": "v%s","target_commitish": "master","name": "v%s","body": "%s","draft": false,"prerelease": false}' "$VERSION" "$VERSION" "$(cat CHANGELOG.md)")
curl --data "$API_JSON" https://api.github.com/repos/saantiaguilera/android-api-graph_flow/releases/releases?access_token=$GITHUB_OAUTH_TOKEN

exit 0