#!/usr/bin/env bash

curl -X POST \
  -H "Content-Type: application/json" \
  http://localhost:8083/connectors \
  --data '{
            "name": "activeMqSourceConnector",
            "config": {
                "connector.class": "io.confluent.connect.activemq.ActiveMQSourceConnector",
                "confluent.topic":"incoming.activemq",
                "kafka.topic":"incoming.activemq",
                "activemq.url":"tcp://faithApps:61616",
                "jms.destination.name":"ColliderToCujo",
                "jms.destination.type":"queue",
                "confluent.topic.bootstrap.servers":"broker:29092",
                "confluent.topic.replication.factor": 1,
                "transforms": "Rename",
                "transforms.Rename.type": "org.apache.kafka.connect.transforms.ReplaceField$Value",
                "transforms.Rename.renames": "properties:jmsProperties"
            }
  }'

#curl -X DELETE http://localhost:8083/connectors/activeMqSourceConnector
