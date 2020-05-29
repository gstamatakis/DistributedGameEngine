from kafka import KafkaAdminClient
from kafka.admin import NewTopic

admin_client = KafkaAdminClient(bootstrap_servers=['kafka1:9094', 'kafka2:9095', 'kafka3:9096'],
                                client_id='test')

topic_list = []

cfg1 = {
    "cleanup.policy": "compact",

    # The amount of time to retain delete tombstone markers for log compacted topics.
    # This setting also gives a bound on the time in which a consumer must complete a read
    # if they begin from offset 0 to ensure that they get a valid snapshot of the final stage
    # (otherwise delete tombstones may be collected before they complete their scan).
    "delete.retention.ms": "100",

    # This configuration controls the period of time after which Kafka will force the log to roll even
    # if the segment file isn't full to ensure that retention can delete or compact old data.
    "segment.ms": "100",

    # The minimum time a message will remain uncompacted in the log.
    # Only applicable for logs that are being compacted.
    "min.compaction.lag.ms": "0",
    "max.compaction.lag.ms": "1",

    # This configuration controls the segment file size for the log.
    # Retention and cleaning is always done a file at a time so a larger segment size means fewer
    # files but less granular control over retention.
    "segment.bytes": "14",

    # This configuration controls how frequently the log compactor will attempt
    # to clean the log (assuming log compaction is enabled). By default we will
    # avoid cleaning a log where more than 50% of the log has been compacted.
    # This ratio bounds the maximum space wasted in the log by duplicates
    # (at 50% at most 50% of the log could be duplicates). A higher ratio will mean fewer,
    # more efficient cleanings but will mean more wasted space in the log.
    # If the max.compaction.lag.ms or the min.compaction.lag.ms configurations are also specified,
    # then the log compactor considers the log to be eligible for compaction as soon as either:
    # (i) the dirty ratio threshold has been met and the log has had dirty (uncompacted)
    # records for at least the min.compaction.lag.ms duration, or
    # (ii) if the log has had dirty (uncompacted) records for at most the max.compaction.lag.ms period.
    "min.cleanable.dirty.ratio": "0.01"
}

cfg2 = {
    "cleanup.policy": "compact",
    "delete.retention.ms": "1",
    "min.compaction.lag.ms": "0",
    "max.compaction.lag.ms": "1",
    "segment.ms": "1",
    "segment.bytes": "128",
    "min.cleanable.dirty.ratio": "0.01"
}

cfg3 = {
    "cleanup.policy": "compact",
    "delete.retention.ms": "1000000000",
    "segment.ms": "1000000",
    "min.compaction.lag.ms": "0",
    "max.compaction.lag.ms": "1",
    "segment.bytes": "100",
    "min.cleanable.dirty.ratio": "0.01"
}

cfg4 = {
    "cleanup.policy": "compact",
    "delete.retention.ms": "1000000000",
    "segment.ms": "10000000",
    "min.compaction.lag.ms": "0",
    "max.compaction.lag.ms": "1",
    "segment.bytes": "100000",
    "min.cleanable.dirty.ratio": "0.01"
}

topic_list.append(
    NewTopic(name="topic1", num_partitions=1, replication_factor=1, topic_configs=cfg2))
topic_list.append(
    NewTopic(name="topic2", num_partitions=1, replication_factor=1, topic_configs=cfg1))
topic_list.append(
    NewTopic(name="topic3", num_partitions=1, replication_factor=2, topic_configs=cfg1))
topic_list.append(
    NewTopic(name="topic4", num_partitions=4, replication_factor=2, topic_configs=cfg1))
topic_list.append(
    NewTopic(name="topic5", num_partitions=1, replication_factor=1, topic_configs=cfg3))
topic_list.append(
    NewTopic(name="topic6", num_partitions=1, replication_factor=1, topic_configs=cfg3))
topic_list.append(
    NewTopic(name="topic7", num_partitions=4, replication_factor=3, topic_configs=cfg3))
topic_list.append(
    NewTopic(name="topic8", num_partitions=4, replication_factor=3, topic_configs=cfg3))  # Good
topic_list.append(
    NewTopic(name="topic9", num_partitions=4, replication_factor=3, topic_configs=cfg4))  # Better

admin_client.create_topics(new_topics=topic_list, validate_only=False)
