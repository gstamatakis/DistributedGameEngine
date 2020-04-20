import json
import time

from kafka import KafkaProducer

print('above producer')
producer = KafkaProducer(bootstrap_servers=['192.168.1.100:9094', '192.168.1.100:9095', '192.168.1.100:9096'],
                         key_serializer=lambda v: json.dumps(v).encode('utf-8'),
                         value_serializer=lambda v: json.dumps(v).encode('utf-8'))

print('after producer')
for i in range(100):
    future = producer.send(topic='topic10',
                           key='Key{0}'.format(i % 10),
                           value='Value{0}'.format(i),
                           partition=0)
    result = future.get(timeout=5)
    future = producer.send(topic='topic10',
                           key='Key{0}'.format(i % 10),
                           value='Value{0}'.format(i),
                           partition=1)
    result = future.get(timeout=5)
    future = producer.send(topic='topic10',
                           key='Key{0}'.format(i % 10),
                           value='Value{0}'.format(i),
                           partition=2)
    result = future.get(timeout=5)
    future = producer.send(topic='topic10',
                           key='Key{0}'.format(i % 10),
                           value='Value{0}'.format(i),
                           partition=3)
    result = future.get(timeout=5)
    time.sleep(0.1)

producer.close(5)

print('after sending messages')
