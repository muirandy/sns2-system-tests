CREATE STREAM ENRICHED_MODIFICATION_INSTRUCTIONS_WITH_SERVICE (
  enrichedInstruction STRUCT <
    operatorId VARCHAR,
    orderId VARCHAR,
    serviceId VARCHAR,
    directoryNumber VARCHAR,
    operatorOrderId VARCHAR,
    features array<VARCHAR>
  >)
WITH (KAFKA_TOPIC='enriched.modification.instructions.with.service', VALUE_FORMAT='JSON');