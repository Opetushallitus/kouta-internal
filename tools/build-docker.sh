#!/bin/bash -e

export ARTIFACT_NAME=kouta-internal
[ $CI ] && export BUILD_ID=ga-$GITHUB_RUN_NUMBER || export BUILD_ID="latest"
[ $CI ] && export DOCKER_TARGET="${ECR_REPO}/${ARTIFACT_NAME}:${BUILD_ID}" || export DOCKER_TARGET="$ARTIFACT_NAME:$BUILD_ID"
[ -z $CI ] && ARTIFACT_DEST_PATH="/usr/local/bin"
[ -z $CI ] && DOCKER_BUILD_DIR="../"

find ${DOCKER_BUILD_DIR}

if [ -d "${DOCKER_BUILD_DIR}/native_libs" ]; then sed -i '/^CMD.*/i COPY native_libs/* /usr/lib' ${DOCKER_BUILD_DIR}/Dockerfile; fi
