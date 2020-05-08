#!/bin/sh

if [ -z "$SENTINEL_MASTER_IP" ]; then
  echo "env var \"SENTINEL_MASTER_IP\" is not set"
  exit 1
fi

sed -i "s/%%SENTINEL_MASTER_IP%%/${SENTINEL_MASTER_IP}/g" /etc/redis/sentinel.conf
sed -i "s/%%SENTINEL_QUORUM%%/${SENTINEL_QUORUM}/g" /etc/redis/sentinel.conf
sed -i "s/%%SENTINEL_DOWN_AFTER%%/${SENTINEL_DOWN_AFTER}/g" /etc/redis/sentinel.conf
sed -i "s/%%SENTINEL_FAILOVER%%/${SENTINEL_FAILOVER}/g" /etc/redis/sentinel.conf

exec docker-entrypoint.sh redis-server /etc/redis/sentinel.conf --sentinel
