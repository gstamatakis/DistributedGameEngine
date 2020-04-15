SENTINEL_IMAGE = dge/redis-sentinel:5
REDIS_IMAGE = dge/redis:5
SPRING_IMAGE = dge/spring:1.0

.PHONY: build
build:
	mvn clean package
	docker image build -t $(SENTINEL_IMAGE) redis-cluster/sentinel/
	docker image build -t $(REDIS_IMAGE) redis-cluster/redis/
	docker image build -t $(SPRING_IMAGE) spring/

.PHONY: push
push:
	docker image push $(SENTINEL_IMAGE)
	docker image push $(REDIS_IMAGE)
	docker image push $(SPRING_IMAGE)
