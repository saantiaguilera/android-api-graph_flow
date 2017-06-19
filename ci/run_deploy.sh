#!/bin/bash

# Modules that will be published
PUBLISH_MODULES=(core conductor views fragments)

./gradlew shouldPublish | sed -f "$STDOUT_FILTERS"

EXIT_CODE=${PIPESTATUS[0]}

if [ $EXIT_CODE -eq 0 ]; then
	for module in ${PUBLISH_MODULES[@]}; do
		./gradlew ${module}:bintrayUpload

		node -e "setTimeout(null,30000)"; # 30 seconds delay

		if [ $? -ne 0 ]; then
			exit 1
		fi
    done

	VERSION=$(grep "versionName = .*" gradle.properties | sed -E "s/versionName = (.*)/\1/g")
	API_JSON=$(printf '{"tag_name": "v%s","target_commitish": "master","name": "v%s","body": "For more information, please see the CHANGELOG.md of this tag","draft": false,"prerelease": false}' "$VERSION" "$VERSION")
	curl --data "$API_JSON" https://api.github.com/repos/saantiaguilera/android-api-graph_flow/releases?access_token=$GITHUB_OAUTH_TOKEN
fi

exit 0