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


Deploy the stack

    docker stack deploy -c docker-compose.yml dge
    

# Docker commands

Stop all services

    docker service rm $(docker service ls -q)
    
    
Stop and remove all containers

    docker stop $(docker ps -a -q)
    docker rm $(docker ps -a -q)


Remove all instances and containers

    docker rm -f $(docker ps -a -q)
    docker rmi -f $(docker images -q)


# Sources
### Authentication

    https://blog.ngopal.com.np/2017/10/10/spring-boot-with-jwt-authentication-using-redis/


### Kafka

    https://github.com/wurstmeister/kafka-docker