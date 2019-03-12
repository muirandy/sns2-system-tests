CREATE STREAM t5_MODIFY_VOICE_FEATURE_MSGS as
SELECT transaction->operatorId AS operatorId,
       transaction->instruction->"order"->operatorOrderId AS operatorOrderId,
       transaction->instruction->"order"->orderId AS orderId,
       transaction->instruction->modifyFeaturesInstruction->serviceId AS serviceId,
       transaction->instruction->modifyFeaturesInstruction->features AS features
FROM INSTRUCTIONS_STREAM_1 WITH (KAFKA_TOPIC='t5_modify_voice_feature_msgs', VALUE_FORMAT='JSON');
