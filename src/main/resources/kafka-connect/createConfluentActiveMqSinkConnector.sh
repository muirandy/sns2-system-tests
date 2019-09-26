#!/usr/bin/env bash

kafkaBroker=${1:-broker:29092}
connectServer=${2:-localhost:8083/connectors}
mqUrl=${3:-tcp://localhost:61616}

echo " --- createConfluentActiveMqSinkConnector.sh --- "

echo $kafkaBroker
echo $connectServer
echo $mqUrl

payload='{
  "name": "AMQSinkConnector",
  "config": {
    "connector.class": "io.confluent.connect.jms.ActiveMqSinkConnector",
    "tasks.max": "1",
    "topics": "switch.modification.instructions",
    "activemq.url": "'
payload+=$mqUrl
payload+='",
    "activemq.username": "connectuser",
    "activemq.password": "connectuser",
    "jms.destination.type": "queue",
    "jms.destination.name": "HaloToKnitware",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "value.converter": "org.apache.kafka.connect.storage.StringConverter",
    "confluent.topic.bootstrap.servers": "'
payload+=$kafkaBroker
payload+='",
    "confluent.topic.replication.factor": "1"
  }
}'

echo $payload

curl -X POST \
  -H "Content-Type: application/json" \
  http://$connectServer \
  --data "$payload"
