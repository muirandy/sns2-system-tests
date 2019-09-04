#!/usr/bin/env bash

curl -X POST \
  -H "Content-Type: application/json" \
  http://localhost:8083/connectors \
  --data '{
            "name": "activeMqSinkConnector",
            "config": {
                "connector.class": "com.aimyourtechnology.kafka.connect.activemq.connector.ActiveMqSinkConnector",
                "activemq.endpoint": "tcp://localhost:61616",
                "activemq.queue": "target-mq-queue",
                "topics": "kafka-topic",
                "kafka.bootstrap.servers": "broker:29092",
            }
  }'

#curl -X DELETE http://localhost:8083/connectors/activeMqSinkConnector
