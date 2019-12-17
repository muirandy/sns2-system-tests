#!/usr/bin/env bash

kafkaBroker=${1:-broker:29092}
connectServer=${2:-localhost:8083/connectors}

payload='{
  "name": "Neo4jSinkConnector",
  "config": {
	"topics": "SINK_MODIFY_VOIP_INSTRUCTIONS_WITH_SWITCH_ID",
	"connector.class": "streams.kafka.connect.sink.Neo4jSinkConnector",
	"errors.retry.timeout": "-1",
	"errors.retry.delay.max.ms": "1000",
	"errors.tolerance": "all",
	"errors.log.enable": true,
	"errors.log.include.messages": true,
	"neo4j.server.uri": "bolt://neo4j:7687",
	"neo4j.authentication.basic.username": "neo4j",
	"neo4j.authentication.basic.password": "connect",
	"neo4j.encryption.enabled": false,
	"neo4j.topic.cypher.my-topic": "MERGE (p:Person{name: event.name, surname: event.surname, from: 'AVRO'}) MERGE (f:Family{name: event.surname}) MERGE (p)-[:BELONGS_TO]->(f)"
  }
}'

echo $payload

curl -X POST \
  -H "Content-Type: application/json" \
  http://$connectServer \
  --data "$payload"


