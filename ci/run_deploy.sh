#!/bin/bash

./gradlew shouldPublish | sed -f "$STDOUT_FILTERS"

EXIT_CODE=${PIPESTATUS[0]}

if [ $EXIT_CODE -eq 0 ]; then
	./gradlew publishModules

	if [ $? -ne 0 ]; then
		exit 1
	fi

	VERSION=$(grep "libraryVersion = .*" gradle.properties | sed -E "s/libraryVersion = (.*)/\1/g")
	API_JSON=$(printf '{"tag_name": "v%s","target_commitish": "master","name": "v%s","body": "","draft": false,"prerelease": false}' "$VERSION" "$VERSION")
	curl --data "$API_JSON" https://api.github.com/repos/saantiaguilera/android-api-graph_flow/releases?access_token=$GITHUB_OAUTH_TOKEN

	exit $?
fi

exit 0