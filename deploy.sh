#!/usr/bin/env bash

make build
# shellcheck disable=SC2046
docker rm $(docker ps --filter "status=exited" -q)
docker image prune -f
docker service update --force dge_spring
