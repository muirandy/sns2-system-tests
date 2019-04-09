#!/usr/bin/env bash

curl -X POST \
  -H "Content-Type: application/json" \
  http://localhost:8083/connectors \
  --data '{
      "name": "services-connector",
      "config": {
        "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
        "tasks.max": 1,
        "connection.user": "sa",
        "connection.url": "jdbc:h2:tcp://h2:9082/foobar",
        "mode": "incrementing",
        "query": "select s.SERVICE_ID, s.SERVICE_SPEC_CODE, dn.DIRECTORY_NUMBER from services s inner join ENDPOINT e on s.ENDPOINT_ID = e.ENDPOINT_ID inner join DN_ALLOCATIONS dn on e.DN_ALLOCATION_ID = dn.DN_ALLOCATION_ID",
        "incrementing.column.name": "SERVICE_ID",
        "topic.prefix": "services",
        "poll.interval.ms": 1000,
        "transforms": "Rename,createKey,extractInt",
        "transforms.Rename.type": "org.apache.kafka.connect.transforms.ReplaceField$Value",
        "transforms.Rename.renames": "SERVICE_ID:serviceId, SERVICE_SPEC_CODE:serviceSpecCode, DIRECTORY_NUMBER:directoryNumber",
        "transforms.createKey.type":"org.apache.kafka.connect.transforms.ValueToKey",
        "transforms.createKey.fields":"serviceId",
        "transforms.extractInt.type":"org.apache.kafka.connect.transforms.ExtractField$Key",
        "transforms.extractInt.field":"serviceId"
      }
    }'

#curl -X DELETE http://localhost:8083/connectors/services-connector