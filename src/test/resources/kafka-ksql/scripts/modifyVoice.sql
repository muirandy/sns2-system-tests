CREATE STREAM INSTRUCTIONS_STREAM_1 (
traceId VARCHAR,
transaction STRUCT <
  "operatorId" VARCHAR,
  "instruction" STRUCT <
    "order" STRUCT <
      "operatorOrderId" VARCHAR,
      "orderId" VARCHAR
    >,
    "modifyFeaturesInstruction" STRUCT <
      "serviceId" VARCHAR,
      "features" STRUCT<
        "feature" array<STRUCT<
          "code" VARCHAR
        >>
      >
    >
  >
>)
WITH (KAFKA_TOPIC='modify.op.msgs', VALUE_FORMAT='JSON');


CREATE STREAM RAW_VOIP_INSTRUCTIONS WITH (PARTITIONS=1,REPLICAS=1)
                                      AS SELECT traceId as "TRACE_ID",
                                                transaction->"operatorId" as "OPERATOR_ID",
                                                transaction->"instruction"->"order"->"operatorOrderId" as "OPERATOR_ORDER_ID",
                                                transaction->"instruction"->"order"->"orderId" as "ORDER_ID",
                                                transaction->"instruction"->"modifyFeaturesInstruction"->"serviceId" as "SERVICE_ID",
                                                transaction->"instruction"->"modifyFeaturesInstruction"->"features"->"feature" as "FEATURES"
                                         FROM INSTRUCTIONS_STREAM_1 WITH (VALUE_FORMAT='JSON');
