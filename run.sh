#Stop everything
docker rm $(docker ps --filter "status=exited" -q)
docker image prune -f
docker swarm leave
docker swarm init

#Setup a private docker registry
docker stop registry
docker rm registry
docker run -d -p 5000:5000 --restart=always --name registry registry:2

#Build local project
mvn clean package -DskipTests=true

#Build docker images and make them accessible to the stack
make build
make push

# Remove a previous stack (if it exists)
docker stack rm dge

#Deploy the stack
docker stack deploy -c swarm.yml dge --with-registry-auth

#Clean up local images
make clean
