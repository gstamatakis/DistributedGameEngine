#Local images
SENTINEL_IMAGE = dge/redis-sentinel:latest
REDIS_IMAGE = dge/redis:latest
GM_IMAGE = dge/game-master:latest
PM_IMAGE = dge/play-master:latest
UI_IMAGE = dge/user-interface:latest

#Remote images
MYSQL_IMAGE = dge/mysql:latest
MYSQL_FAILOVER_IMAGE = dge/mysql-failover:latest
MYSQL_ROUTER_IMAGE = dge/mysql-router:latest
ZOOKEEPER_IMAGE = dge/zookeeper:latest
KAFKA_IMAGE = dge/kafka:latest

# Docker local private registry
REGISTRY_HOST = 127.0.0.1:5000

#Default action will build and push everything to the repo and then remove local images
all: build push clean

build:
	docker image build -t $(SENTINEL_IMAGE) docker/redis-cluster/sentinel/
	docker image build -t $(REDIS_IMAGE) docker/redis-cluster/redis/
	docker image build -t $(GM_IMAGE) game-master/
	docker image build -t $(PM_IMAGE) play-master/
	docker image build -t $(UI_IMAGE) user-interface/
	docker pull robinong79/mysql_repl:5.7
	docker pull robinong79/mysqlfailover:1.6.4
	docker pull robinong79/mysqlrouter:2.0.4
	docker pull wurstmeister/zookeeper
	docker pull wurstmeister/kafka:2.12-2.4.1

push:
	docker tag $(SENTINEL_IMAGE) $(REGISTRY_HOST)/$(SENTINEL_IMAGE)
	docker tag $(REDIS_IMAGE) $(REGISTRY_HOST)/$(REDIS_IMAGE)
	docker tag $(GM_IMAGE) $(REGISTRY_HOST)/$(GM_IMAGE)
	docker tag $(PM_IMAGE) $(REGISTRY_HOST)/$(PM_IMAGE)
	docker tag $(UI_IMAGE) $(REGISTRY_HOST)/$(UI_IMAGE)
	docker tag robinong79/mysql_repl:5.7 $(REGISTRY_HOST)/$(MYSQL_IMAGE)
	docker tag robinong79/mysqlfailover:1.6.4 $(REGISTRY_HOST)/$(MYSQL_FAILOVER_IMAGE)
	docker tag robinong79/mysqlrouter:2.0.4 $(REGISTRY_HOST)/$(MYSQL_ROUTER_IMAGE)
	docker tag wurstmeister/zookeeper $(REGISTRY_HOST)/$(ZOOKEEPER_IMAGE)
	docker tag wurstmeister/kafka:2.12-2.4.1 $(REGISTRY_HOST)/$(KAFKA_IMAGE)
	docker push $(REGISTRY_HOST)/$(SENTINEL_IMAGE) --disable-content-trust
	docker push $(REGISTRY_HOST)/$(REDIS_IMAGE) --disable-content-trust
	docker push $(REGISTRY_HOST)/$(GM_IMAGE) --disable-content-trust
	docker push $(REGISTRY_HOST)/$(PM_IMAGE) --disable-content-trust
	docker push $(REGISTRY_HOST)/$(UI_IMAGE) --disable-content-trust
	docker push $(REGISTRY_HOST)/$(MYSQL_IMAGE) --disable-content-trust
	docker push $(REGISTRY_HOST)/$(MYSQL_FAILOVER_IMAGE) --disable-content-trust
	docker push $(REGISTRY_HOST)/$(MYSQL_ROUTER_IMAGE) --disable-content-trust
	docker push $(REGISTRY_HOST)/$(ZOOKEEPER_IMAGE) --disable-content-trust
	docker push $(REGISTRY_HOST)/$(KAFKA_IMAGE) --disable-content-trust

update-local:
	docker image build -t $(GM_IMAGE) game-master/
	docker image build -t $(PM_IMAGE) play-master/
	docker image build -t $(UI_IMAGE) user-interface/
	docker tag $(UI_IMAGE) $(REGISTRY_HOST)/$(UI_IMAGE)
	docker tag $(GM_IMAGE) $(REGISTRY_HOST)/$(GM_IMAGE)
	docker tag $(PM_IMAGE) $(REGISTRY_HOST)/$(PM_IMAGE)
	docker push $(REGISTRY_HOST)/$(UI_IMAGE) --disable-content-trust
	docker push $(REGISTRY_HOST)/$(GM_IMAGE) --disable-content-trust
	docker push $(REGISTRY_HOST)/$(PM_IMAGE) --disable-content-trust
	docker image remove $(GM_IMAGE)
	docker image remove $(PM_IMAGE)
	docker image remove $(UI_IMAGE)
	docker image remove openjdk:8-jdk-alpine

clean:
	docker image remove $(GM_IMAGE)
	docker image remove $(PM_IMAGE)
	docker image remove $(UI_IMAGE)
	docker image remove $(SENTINEL_IMAGE)
	docker image remove $(REDIS_IMAGE)
	docker image remove robinong79/mysql_repl:5.7
	docker image remove robinong79/mysqlfailover:1.6.4
	docker image remove robinong79/mysqlrouter:2.0.4
	docker image remove wurstmeister/zookeeper
	docker image remove wurstmeister/kafka:2.12-2.4.1
	docker image remove openjdk:8-jdk-alpine
	docker image remove redis:5
