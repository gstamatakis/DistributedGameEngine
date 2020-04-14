## Docker
Build (while inside this module)

     docker build -t spring-app:1.0 .
     
Run

    docker run -d -p 8080:8080 -t spring-app:1.0
    
Join swarm as a service

    docker service create --name spring-app --network redis_redis spring-app:1.0


