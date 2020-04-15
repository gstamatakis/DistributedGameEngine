SENTINEL_IMAGE = dge/redis-sentinel:5
REDIS_IMAGE = dge/redis:5
GM_IMAGE = dge/game-master:1.0
PM_IMAGE = dge/play-master:1.0
UI_IMAGE = dge/user-interface:1.0

.PHONY: build
build:
	docker image build -t $(SENTINEL_IMAGE) redis-cluster/sentinel/
	docker image build -t $(REDIS_IMAGE) redis-cluster/redis/
	docker image build -t $(GM_IMAGE) game-master/
	docker image build -t $(PM_IMAGE) play-master/
	docker image build -t $(UI_IMAGE) user-interface/

.PHONY: push
push:
	docker image push $(SENTINEL_IMAGE)
	docker image push $(REDIS_IMAGE)
	docker image push $(GM_IMAGE)
	docker image push $(PM_IMAGE)
	docker image push $(UI_IMAGE)
