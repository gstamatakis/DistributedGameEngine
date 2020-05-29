from kafka import KafkaConsumer

consumer = KafkaConsumer('testtopic',
                         # group_id='g1',
                         bootstrap_servers=['kafka1:9094', 'kafka2:9095', 'kafka3:9096'],
                         auto_offset_reset='earliest',
                         enable_auto_commit=True,
                         auto_commit_interval_ms=1000
                         )

print('Consuming messages')
for msg in consumer:
    print(msg)
