FROM redis:5

COPY --chown=redis:redis sentinel.conf /etc/redis/sentinel.conf
COPY entrypoint.sh /usr/local/bin/

ENV SENTINEL_QUORUM 2
ENV SENTINEL_DOWN_AFTER 5000
ENV SENTINEL_FAILOVER 15000

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]