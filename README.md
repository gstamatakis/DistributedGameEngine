## Swarm 

Start (or join) a Docker Swarm

    docker swarm init --advertise-addr 83.212.102.12
    
    
Build the images

    make
    
    
Deploy the stack

    docker stack deploy -c docker-compose.yml dge
    
    
# Sources
## Authentication

    https://blog.ngopal.com.np/2017/10/10/spring-boot-with-jwt-authentication-using-redis/


# Docker cmd


Stop and remove all containers

    docker stop $(docker ps -a -q)
    docker rm $(docker ps -a -q)


Remove all instances and containers

    docker rm -f $(docker ps -a -q)
    docker rmi -f $(docker images -q)


Stop all services

        docker service rm $(docker service ls -q)
