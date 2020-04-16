## Docker
Build (while inside this module)

     docker build -t spring:1.0 .
     
     
Run

    docker run -d -p 8080:8080 -t spring:1.0
    
    
Join swarm as a service

    docker service create --name spring --network redis_redis spring:1.0


Allow service port

    docker service update --publish-add 8080 spring


Stack deploy

    docker stack deploy -c full.yml app