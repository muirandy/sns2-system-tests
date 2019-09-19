CREATE STREAM RAW_VOIP_INSTRUCTIONS WITH (PARTITIONS=4,REPLICAS=1,VALUE_FORMAT='JSON')
AS SELECT traceId as "TRACE_ID",
       transaction->"operatorId" as "OPERATOR_ID",
       transaction->"instruction"->"order"->"operatorOrderId" as "OPERATOR_ORDER_ID",
       transaction->"instruction"->"order"->"orderId" as "ORDER_ID",
       transaction->"instruction"->"modifyFeaturesInstruction"->"serviceId" as "SERVICE_ID",
       transaction->"instruction"->"modifyFeaturesInstruction"->"features"->"feature" as "FEATURES"
FROM INSTRUCTIONS_STREAM_1;