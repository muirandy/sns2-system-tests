CREATE STREAM FROM_VOIP_SWITCH_SERVICES (
  serviceId BIGINT,
  switchServiceId BIGINT
)
WITH (KAFKA_TOPIC='voip-switch-services', VALUE_FORMAT='JSON');


CREATE TABLE T_FROM_VOIP_SWITCH_SERVICES (
  serviceId BIGINT,
  switchServiceId BIGINT
)
WITH (KAFKA_TOPIC='voip-switch-services', VALUE_FORMAT='JSON', KEY='serviceId');
