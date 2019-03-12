CREATE STREAM INSTRUCTIONS_STREAM_1 (transaction STRUCT <
  operatorId VARCHAR,
  instruction STRUCT <
    "order" STRUCT <
      operatorOrderId VARCHAR,
      orderId VARCHAR
    >,
    modifyFeaturesInstruction STRUCT <
      serviceId VARCHAR,
      features STRUCT<
        feature array<STRUCT<
          code VARCHAR
        >>
      >
    >
  >
>)
WITH (KAFKA_TOPIC='modify.op.msgs', VALUE_FORMAT='JSON');
