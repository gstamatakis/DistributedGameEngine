#!/usr/bin/env bash

#Stop everything
docker rm $(docker ps --filter "status=exited" -q)
docker image prune -f

#Build everything
mvn clean package
make build