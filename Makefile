#Local images
SENTINEL_IMAGE = dge/redis-sentinel:1.0
REDIS_IMAGE = dge/redis:1.0
GM_IMAGE = dge/game-master:1.0
PM_IMAGE = dge/play-master:1.0
UI_IMAGE = dge/user-interface:1.0

#Remote images
MYSQL_IMAGE = dge/mysql:1.0
MYSQL_FAILOVER_IMAGE = dge/mysql-failover:1.0
MYSQL_ROUTER_IMAGE = dge/mysql-router:1.0
ZOOKEEPER_IMAGE = dge/zookeeper:1.0
KAFKA_IMAGE = dge/kafka:1.0

REGISTRY_HOST = localhost:5000

build:
	#Build local images
	docker image build -t $(SENTINEL_IMAGE) docker/redis-cluster/sentinel/
	docker image build -t $(REDIS_IMAGE) docker/redis-cluster/redis/
	docker image build -t $(GM_IMAGE) game-master/
	docker image build -t $(PM_IMAGE) play-master/
	docker image build -t $(UI_IMAGE) user-interface/

	#Pull remote images
	docker pull robinong79/mysql_repl:5.7
	docker pull robinong79/mysqlfailover:1.6.4
	docker pull robinong79/mysqlrouter:2.0.4
	docker pull wurstmeister/zookeeper
	docker pull wurstmeister/kafka:2.12-2.4.1

push:
	#Local images
	docker tag $(SENTINEL_IMAGE) $(REGISTRY_HOST)/$(SENTINEL_IMAGE)
	docker push $(REGISTRY_HOST)/$(SENTINEL_IMAGE) --disable-content-trust
	docker tag $(REDIS_IMAGE) $(REGISTRY_HOST)/$(REDIS_IMAGE)
	docker push $(REGISTRY_HOST)/$(REDIS_IMAGE) --disable-content-trust
	docker tag $(GM_IMAGE) $(REGISTRY_HOST)/$(GM_IMAGE)
	docker push $(REGISTRY_HOST)/$(GM_IMAGE) --disable-content-trust
	docker tag $(PM_IMAGE) $(REGISTRY_HOST)/$(PM_IMAGE)
	docker push $(REGISTRY_HOST)/$(PM_IMAGE) --disable-content-trust
	docker tag $(UI_IMAGE) $(REGISTRY_HOST)/$(UI_IMAGE)
	docker push $(REGISTRY_HOST)/$(UI_IMAGE) --disable-content-trust

	#Remote images
	docker tag robinong79/mysql_repl:5.7 $(REGISTRY_HOST)/$(MYSQL_IMAGE)
	docker push $(REGISTRY_HOST)/$(MYSQL_IMAGE) --disable-content-trust
	docker tag robinong79/mysqlfailover:1.6.4 $(REGISTRY_HOST)/$(MYSQL_FAILOVER_IMAGE)
	docker push $(REGISTRY_HOST)/$(MYSQL_FAILOVER_IMAGE) --disable-content-trust
	docker tag robinong79/mysqlrouter:2.0.4 $(REGISTRY_HOST)/$(MYSQL_ROUTER_IMAGE)
	docker push $(REGISTRY_HOST)/$(MYSQL_ROUTER_IMAGE) --disable-content-trust
	docker tag wurstmeister/zookeeper $(REGISTRY_HOST)/$(ZOOKEEPER_IMAGE)
	docker push $(REGISTRY_HOST)/$(ZOOKEEPER_IMAGE) --disable-content-trust
	docker tag wurstmeister/kafka:2.12-2.4.1 $(REGISTRY_HOST)/$(KAFKA_IMAGE)
	docker push $(REGISTRY_HOST)/$(KAFKA_IMAGE) --disable-content-trust

clean:
	#Remove local images
	docker image remove $(SENTINEL_IMAGE)
	docker image remove $(REDIS_IMAGE)
	docker image remove $(GM_IMAGE)
	docker image remove $(PM_IMAGE)
	docker image remove $(UI_IMAGE)

	# Delete pulled images
	docker image remove robinong79/mysql_repl:5.7
	docker image remove robinong79/mysqlfailover:1.6.4
	docker image remove robinong79/mysqlrouter:2.0.4
	docker image remove wurstmeister/zookeeper
	docker image remove wurstmeister/kafka:2.12-2.4.1

	#Delte remaining local images
	docker image remove openjdk:8-jdk-alpine
	docker image remove redis:5

