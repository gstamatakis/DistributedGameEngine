#Stop everything
docker rm $(docker ps --filter "status=exited" -q)
docker image prune -f

#Build local project
mvn clean package -DskipTests=true

#Build docker images and make them accessible to the stack
make build
make tag

# Remove a previous stack (if it exists)
docker stack rm dge

#Deploy the stack
docker stack deploy -c swarm.yml dge --with-registry-auth

#Clean up local images
make clean
