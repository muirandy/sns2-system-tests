#!/usr/bin/env bash

curl -X POST -H "Content-Type: application/json" \
http://localhost:8083/connectors \
  -d '{
 "name": "elasticsearch-sink",
  "config": {
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "tasks.max": "1",
    "topics": "modifyOperatorMessagesTopic,sinkModifyVoipInstructionsWithSwitchIdTopic",
    "key.ignore": "true",
    "topic.schema.ignore":"true",
    "schema.ignore":"true",
    "connection.url": "http://elasticsearch:9200",
    "type.name": "kafka-connect",
    "name": "elasticsearch-sink"
  }
}'

#curl -X DELETE http://localhost:8083/connectors/elasticsearch-sink