# Swarm 

Start (or join) a Docker Swarm

    docker swarm init --advertise-addr 83.212.102.12
    
#Build and Run
The following instruction can be used to deploy the entire application in a Docker
Swarm environment. The following steps assume that the path is at the top-level
directory of the project (same as this README).

Build the project
    
    mvn clean package 


Build the docker images 
    
    make
    
    
Set the redis flags

    docker node update --label-add redis-master=true docker-desktop
    docker node update --label-add redis-replica=true docker-desktop
    docker node update --label-add redis-sentinel=true docker-desktop
    docker node update --label-add spring=true docker-desktop
    docker node update --label-add kafka=true docker-desktop
    docker node update --label-add zookeeper=true docker-desktop
    docker node update --label-add mysql=true docker-desktop


Deploy the FULL stack

    docker stack deploy -c swarm.yml dge

    
Deploy just the Databases and Kafka (useful for testing)

    docker stack deploy -c auxiliary.yml dge

    
# Docker commands (run from PowerShell)

Stop a deployed stack

    docker stack rm dge
    

Update the service images 

    docker service update --force dge_game-master


Stop all services

    docker service rm $(docker service ls -q)
    
    
Stop and remove all EXITED containers

    docker rm $(docker ps --filter "status=exited" -q)


Remove all unused images

    docker image prune -f
    

Stop and remove all containers

    docker stop $(docker ps -a -q)
    docker rm $(docker ps -a -q)


Remove all instances and containers

    docker rm -f $(docker ps -a -q)
    docker rmi -f $(docker images -q)

# Registry

Deploy a Docker registry so other nodes can access your images

    docker service create --name registry --publish published=5000,target=5000 registry:2

Verify that it works (should return '{}')

    curl http://localhost:5000/v2/
    
Push something

    make build 
    make push
    

# Sources
### Authentication

    https://blog.ngopal.com.np/2017/10/10/spring-boot-with-jwt-authentication-using-redis/

    https://medium.com/@xoor/jwt-authentication-service-44658409e12c
    
### Kafka

    https://github.com/wurstmeister/kafka-docker
    
### MySQL

    https://github.com/robinong79/docker-swarm-mysql-masterslave-failover