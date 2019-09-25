CREATE TABLE T_FROM_SERVICES
(
    serviceId       BIGINT,
    serviceSpecCode VARCHAR,
    directoryNumber VARCHAR
)
WITH (KAFKA_TOPIC='services', VALUE_FORMAT='JSON', KEY ='serviceId');
