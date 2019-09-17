#!/usr/bin/env bash

kafkaBroker=${1:-broker:29092}
connectServer=${2:-localhost:8083/connectors}
mqUrl=${3:-tcp://localhost:61616}

echo $kafkaBroker
echo $connectServer
echo $mqUrl

echo " --- createActiveMqSinkConnector.sh --- "

payload='{
            "name": "activeMqSinkConnector",
            "config": {
                "connector.class": "com.aimyourtechnology.kafka.connect.activemq.connector.ActiveMqSinkConnector",
                "activemq.endpoint": "'
payload+=$mqUrl
payload+='",
                "activemq.queue": "target-mq-queue",
                "topics": "kafka-topic",
                "kafka.bootstrap.servers": "'
payload+=$kafkaBroker
payload+='"
            }
  }'

echo $payload


curl -X POST \
  -H "Content-Type: application/json" \
  http://$connectServer \
  --data '$payload'



#curl -X DELETE http://localhost:8083/connectors/activeMqSinkConnector
