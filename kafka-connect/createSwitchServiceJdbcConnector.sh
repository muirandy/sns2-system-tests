#!/usr/bin/env bash

curl -X POST \
  -H "Content-Type: application/json" \
  http://localhost:8083/connectors \
  --data '{
      "name": "switch-services-connector",
      "config": {
        "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
        "tasks.max": 1,
        "connection.user": "sa",
        "connection.url": "jdbc:h2:tcp://h2:9082/foobar",
        "mode": "incrementing",
        "query": "Select SERVICE_ID, SWITCH_SERVICE_ID from VOIP_SERVICE_DETAILS",
        "incrementing.column.name": "SERVICE_ID",
        "topic.prefix": "voip-switch-services",
        "poll.interval.ms": 1000,
        "transforms": "Rename,createKey,extractInt",
        "transforms.Rename.type": "org.apache.kafka.connect.transforms.ReplaceField$Value",
        "transforms.Rename.renames": "SERVICE_ID:serviceId, SWITCH_SERVICE_ID:switchServiceId",
        "transforms.createKey.type":"org.apache.kafka.connect.transforms.ValueToKey",
        "transforms.createKey.fields":"serviceId",
        "transforms.extractInt.type":"org.apache.kafka.connect.transforms.ExtractField$Key",
        "transforms.extractInt.field":"serviceId"
      }
    }'

#curl -X DELETE http://localhost:8083/connectors/services-connector