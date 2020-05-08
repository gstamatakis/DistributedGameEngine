#!/usr/bin/env bash

#Start the registry
docker service create --name registry --publish published=5000,target=5000 registry:2

#Push everything to the registry
make push

#Clean up local images
make clean

#Start the swarm
docker stack deploy -c swarm.yml dge --with-registry-auth
