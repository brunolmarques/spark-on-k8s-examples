#!/bin/bash

IMAGE_NAME=gitlab-registry.cern.ch/db/spark-service/spark-k8s-examples
IMAGE_TAG=v2.4.0-hadoop3.1-examples

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

docker build \
  --build-arg BUILD_DATE=$(date -u +"%Y-%m-%dT%H:%M:%SZ") \
  -t ${IMAGE_NAME}:${IMAGE_TAG} $DIR