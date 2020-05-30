#!/usr/bin/env bash

# Stop the stack
docker stack rm dge

# Stop the registry
docker stop registry
docker rm registry
docker service rm registry