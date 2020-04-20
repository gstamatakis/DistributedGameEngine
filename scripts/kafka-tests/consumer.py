from kafka import KafkaConsumer

consumer = KafkaConsumer('output-topic',
                         group_id='g1',
                         bootstrap_servers=['192.168.1.100:9094', '192.168.1.100:9095', '192.168.1.100:9096'],
                         auto_offset_reset='earliest',
                         enable_auto_commit=True,
                         auto_commit_interval_ms=1000
                         )

print('Consuming messages')
for msg in consumer:
    print(msg)
