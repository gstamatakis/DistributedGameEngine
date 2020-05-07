from kafka import KafkaConsumer

consumer = KafkaConsumer('practice',
                         # group_id='g1',
                         bootstrap_servers=['127.0.0.1:9094', '127.0.0.1:9095', '127.0.0.1:9096'],
                         auto_offset_reset='earliest',
                         enable_auto_commit=True,
                         auto_commit_interval_ms=1000
                         )

print('Consuming messages')
for msg in consumer:
    print(msg)
