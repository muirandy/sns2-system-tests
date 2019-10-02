#!/bin/bash

kafkaBrokerContainer=${1:-broker}

echo " --- setupKafka.sh --- "
echo $kafkaBrokerContainer

docker exec $kafkaBrokerContainer kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic incoming.activemq
docker exec $kafkaBrokerContainer kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic INCOMING_OP_MSGS
docker exec $kafkaBrokerContainer kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic modify.op.msgs
docker exec $kafkaBrokerContainer kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic enriched.modification.instructions
docker exec $kafkaBrokerContainer kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic enriched.modification.instructions.with.dn
docker exec $kafkaBrokerContainer kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic switch.modification.instructions
docker exec $kafkaBrokerContainer kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic services
docker exec $kafkaBrokerContainer kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic voip-switch-services
docker exec $kafkaBrokerContainer kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic MODIFY_VOICE_FEATURE_MSGS
docker exec $kafkaBrokerContainer kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic RAW_VOIP_INSTRUCTIONS
docker exec $kafkaBrokerContainer kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic XSLT
docker exec $kafkaBrokerContainer kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic XML_SINK_MODIFY_VOIP_INSTRUCTIONS_WITH_SWITCH_ID
docker exec $kafkaBrokerContainer kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic AUDIT
docker exec $kafkaBrokerContainer kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic KNITWARE_ERRORS_XML
docker exec $kafkaBrokerContainer kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic KNITWARE_ERRORS
