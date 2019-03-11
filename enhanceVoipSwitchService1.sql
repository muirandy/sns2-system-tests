CREATE STREAM VOIP_SERVICE_DETAILS (
  serviceId BIGINT,
  switchServiceId BIGINT
)
WITH (KAFKA_TOPIC='voip-switch-services', VALUE_FORMAT='JSON');
