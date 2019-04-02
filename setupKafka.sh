#!/bin/bash

cd ../kafka_2.12-2.1.0
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 4 --topic incoming.op.msgs
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 4 --topic modify.op.msgs
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 4 --topic enriched.modification.instructions
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 4 --topic enriched.modification.instructions.with.dn
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 4 --topic switch.modification.instructions
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 4 --topic services
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 4 --topic voip-switch-services
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 4 --topic MODIFY_VOICE_FEATURE_MSGS
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 4 --topic RAW_VOIP_INSTRUCTIONS
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 4 --topic AUDIT
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 4 --topic KNITWARE_ERRORS_XML
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 4 --topic KNITWARE_ERRORS
