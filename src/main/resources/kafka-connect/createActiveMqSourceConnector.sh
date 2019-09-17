#!/usr/bin/env bash

kafkaBroker=${1:-broker:29092}
connectServer=${2:-localhost:8083/connectors}
mqUrl=${3:-tcp://faithApps:61616}

echo " --- createActiveMqSourceConnector.sh --- "

echo $kafkaBroker
echo $connectServer
echo $mqUrl

payload='{
            "name": "activeMqSourceConnector",
            "config": {
                "connector.class": "io.confluent.connect.activemq.ActiveMQSourceConnector",
                "confluent.topic":"incoming.activemq",
                "kafka.topic":"incoming.activemq",
                "activemq.url":"'
payload+=$mqUrl
payload+='",
                "jms.destination.name":"ColliderToCujo",
                "jms.destination.type":"queue",
                "confluent.topic.bootstrap.servers":"'
payload+=${kafkaBroker}
payload+='",
                "confluent.topic.replication.factor": 1,
                "transforms": "Rename",
                "transforms.Rename.type": "org.apache.kafka.connect.transforms.ReplaceField$Value",
                "transforms.Rename.renames": "properties:jmsProperties"
            }
  }'

echo $payload

curl -X POST \
  -H "Content-Type: application/json" \
  http://$connectServer \
  --data "$payload"

#curl -X DELETE http://localhost:8083/connectors/activeMqSourceConnector
