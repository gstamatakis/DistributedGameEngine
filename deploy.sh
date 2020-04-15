#!/usr/bin/env bash

mvn clean package
make build
# shellcheck disable=SC2046
docker rm $(docker ps --filter "status=exited" -q)
docker image prune -f
docker service update --force dge_spring
docker stack deploy -c docker-compose.yml dge
