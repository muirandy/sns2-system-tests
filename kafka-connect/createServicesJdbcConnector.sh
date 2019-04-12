#!/usr/bin/env bash

curl -X POST \
  -H "Content-Type: application/json" \
  http://localhost:8083/connectors \
  --data '{
      "name": "services-connector6",
      "config": {
        "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
        "tasks.max": 1,
        "connection.user": "system",
        "connection.password": "oracle",
        "connection.url": "jdbc:oracle:thin:@//faithDB:1521/db1",
        "mode": "timestamp",
        "query": "select CREATED_DTM, SERVICE_ID, SERVICE_SPEC_CODE, DIRECTORY_NUMBER from SERVICE_OWNER.andy1",
        "timestamp.column.name": "CREATED_DTM",
        "topic.prefix": "services",
        "numeric.mapping":"best_fit",
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

#Oracle View DDL:
#create view SERVICE_OWNER.ANDY1 as select s.CREATED_DTM, CAST (s.SERVICE_ID as NUMERIC(8,0)) SERVICE_ID, s.SERVICE_SPEC_CODE, dn.DIRECTORY_NUMBER from SERVICE_OWNER.services s inner join SERVICE_OWNER.ENDPOINT e on s.ENDPOINT_ID = e.ENDPOINT_ID inner join SERVICE_OWNER.DN_ALLOCATIONS dn on e.DN_ALLOCATION_ID = dn.DN_ALLOCATION_ID