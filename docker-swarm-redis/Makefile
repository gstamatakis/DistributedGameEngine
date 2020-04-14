SENTINEL_IMAGE = kumojin/redis-sentinel:5
REDIS_IMAGE = kumojin/redis:5

.PHONY: build
build:
	docker image build -t $(SENTINEL_IMAGE) sentinel/
	docker image build -t $(REDIS_IMAGE) redis/

.PHONY: push
push:
	docker image push $(SENTINEL_IMAGE)
	docker image push $(REDIS_IMAGE)