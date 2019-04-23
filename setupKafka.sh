#!/bin/bash

docker exec -it broker kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic incoming.activemq
docker exec -it broker kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic INCOMING_OP_MSGS
docker exec -it broker kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic modify.op.msgs
docker exec -it broker kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic enriched.modification.instructions
docker exec -it broker kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic enriched.modification.instructions.with.dn
docker exec -it broker kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic switch.modification.instructions
docker exec -it broker kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic services
docker exec -it broker kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic voip-switch-services
docker exec -it broker kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic MODIFY_VOICE_FEATURE_MSGS
docker exec -it broker kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic RAW_VOIP_INSTRUCTIONS
docker exec -it broker kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic AUDIT
docker exec -it broker kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic KNITWARE_ERRORS_XML
docker exec -it broker kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic KNITWARE_ERRORS
