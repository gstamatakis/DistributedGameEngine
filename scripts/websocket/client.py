import random

import stomper
import websocket

if __name__ == '__main__':
    websocket.enableTrace(True)

    # Connecting to websocket
    ws = websocket.create_connection("ws://localhost:8080/endpoint1", http_proxy_auth="Bearer: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImF1dGgiOlt7ImF1dGhvcml0eSI6IlJPTEVfQURNSU4ifV0sImlhdCI6MTU4ODU4ODc1MywiZXhwIjoxNTg4NTg5MDUzfQ.IVnZQ3ifwTcGpJDkBc4cAmFlvs1ESI_J4NqklSuhWMY")
    print("Connected")

    # # Subscribing to topic
    ws.send(stomper.subscribe("/app/out1", str(random.randint(0, 1000)), ack='auto'))
    print("Subscribed")

    # # Sending some message
    ws.send(stomper.send("/app/in1", "Hello there"))

    while True:
        print("Receiving data: ")
        d = ws.recv()
        print(d)
