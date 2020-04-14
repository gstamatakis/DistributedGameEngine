# Docker Swarm Redis

https://redis.io/topics/sentinel#sentinel-docker-nat-and-possible-issues

## Setup
### Docker nodes configuration
#### Master node

```shell
docker node update --label-add redis-master=true docker-desktop
```

#### Replica nodes

```shell
docker node update --label-add redis-replica=true docker-desktop
```

#### Sentinel nodes

```shell
docker node update --label-add redis-sentinel=true docker-desktop
```

### Deploy stack

```shell
docker stack deploy -c redis.yml redis
```
