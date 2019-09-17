#!/usr/bin/env bash

echo " --- createSwitchServiceJdbcConnector.sh --- "

curl -X POST \
  -H "Content-Type: application/json" \
  http://localhost:8083/connectors \
  --data '{
      "name": "switch-services-connector",
      "config": {
        "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
        "tasks.max": 1,
        "connection.user": "system",
        "connection.password": "oracle",
        "connection.url": "jdbc:oracle:thin:@//faithDB:1521/db1",
        "mode": "incrementing",
        "query": "Select CAST (SERVICE_ID as NUMBER(8,0)) SERVICE_ID, CAST (SWITCH_SERVICE_ID as NUMBER(8,0)) SWITCH_SERVICE_ID from SERVICE_OWNER.VOIP_SERVICE_DETAILS",
        "incrementing.column.name": "SERVICE_ID",
        "numeric.mapping":"best_fit",
        "topic.prefix": "voip-switch-services",
        "poll.interval.ms": 1000,
        "debug": true,
        "transforms": "Rename,createKey,extractInt",
        "transforms.Rename.type": "org.apache.kafka.connect.transforms.ReplaceField$Value",
        "transforms.Rename.renames": "SERVICE_ID:serviceId, SWITCH_SERVICE_ID:switchServiceId",
        "transforms.createKey.type":"org.apache.kafka.connect.transforms.ValueToKey",
        "transforms.createKey.fields":"serviceId",
        "transforms.extractInt.type":"org.apache.kafka.connect.transforms.ExtractField$Key",
        "transforms.extractInt.field":"serviceId"
      }
    }'

#curl -X DELETE http://localhost:8083/connectors/switch-services-connector