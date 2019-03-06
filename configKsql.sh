#!/usr/bin/env bash
curl -X "POST" "http://localhost:8088/ksql" \
     -H "Content-Type: application/vnd.ksql.v1+json; charset=utf-8" \
     -d $'{
  "ksql": "CREATE STREAM a4_modify_voice_feature_msgs as SELECT transaction->operatorId AS operatorId, transaction->instruction->order->operatorOrderId AS operatorOrderId FROM INSTRUCTIONS_STREAM_1 WITH (VALUE_FORMAT=JSON);",
  "streamsProperties": {"ksql.streams.auto.offset.reset": "earliest"}
}'

curl -X "POST" "http://localhost:8088/ksql" \
     -H "Content-Type: application/vnd.ksql.v1+json; charset=utf-8" \
     -d $'{
  "ksql": "CREATE STREAM INSTRUCTIONS_STREAM_2 (transaction STRUCT <operatorId VARCHAR, instruction STRUCT <order STRUCT <operatorOrderId VARCHAR, orderId VARCHAR>, modifyFeaturesInstruction STRUCT <serviceId VARCHAR, features STRUCT<feature array<STRUCT<code VARCHAR>>>>>>) WITH (KAFKA_TOPIC='test_modify_op_msgs', VALUE_FORMAT=JSON);",
  "streamsProperties": {"ksql.streams.auto.offset.reset": "earliest"}
}'



#  "ksql": "CREATE STREAM test5_modify_voice_feature_msgs as SELECT transaction->operatorId AS operatorId, transaction->instruction->order->operatorOrderId AS operatorOrderId FROM INSTRUCTIONS_STREAM_1 WITH (KAFKA_TOPIC=\'test5.modify.voice.feature.msgs\', VALUE_FORMAT=\'JSON\');",
#  "streamsProperties": {"ksql.streams.auto.offset.reset": "earliest"}
#  "ksql": "CREATE STREAM "test4.modify.voice.feature.msgs" as SELECT transaction->operatorId AS "operatorId", transaction->instruction->order->operatorOrderId AS "operatorOrderId", transaction->instruction->order->orderId AS "orderId", transaction->instruction->modifyFeaturesInstruction->serviceId AS "serviceId", transaction->instruction->modifyFeaturesInstruction->features AS "features" FROM INSTRUCTIONS_STREAM_1 WITH (KAFKA_TOPIC='modify.voice.feature.msgs', VALUE_FORMAT='JSON')",
#  "ksql": "CREATE STREAM INSTRUCTIONS_STREAM_2 (transaction STRUCT <operatorId VARCHAR, instruction STRUCT <order STRUCT <operatorOrderId VARCHAR, orderId VARCHAR>, modifyFeaturesInstruction STRUCT <serviceId VARCHAR, features STRUCT<feature array<STRUCT<code VARCHAR>>>>>>) WITH (KAFKA_TOPIC='modify.op.msgs', VALUE_FORMAT='JSON')",
