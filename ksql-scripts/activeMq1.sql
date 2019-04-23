CREATE STREAM STREAM_FROM_INCOMING_ACTIVEMQ (
text VARCHAR,
jmsProperties STRUCT <
  "NetstreamMessageType" STRUCT <
    "propertyType" VARCHAR,
    "boolean" BOOLEAN,
    "byte" VARCHAR,
    "short" INTEGER,
    "integer" INTEGER,
    "long" BIGINT,
    "float" DOUBLE,
    "double" DOUBLE,
    "string" VARCHAR
  >,
  "Message-Id" STRUCT <
    "propertyType" VARCHAR,
    "boolean" BOOLEAN,
    "byte" VARCHAR,
    "short" INTEGER,
    "integer" INTEGER,
    "long" BIGINT,
    "float" DOUBLE,
    "double" DOUBLE,
    "string" VARCHAR
  >,
  "source" STRUCT <
    "propertyType" VARCHAR,
    "boolean" BOOLEAN,
    "byte" VARCHAR,
    "short" INTEGER,
    "integer" INTEGER,
    "long" BIGINT,
    "float" DOUBLE,
    "double" DOUBLE,
    "string" VARCHAR
  >,
  "TraceyId" STRUCT <
    "propertyType" VARCHAR,
    "boolean" BOOLEAN,
    "byte" VARCHAR,
    "short" INTEGER,
    "integer" INTEGER,
    "long" BIGINT,
    "float" DOUBLE,
    "double" DOUBLE,
    "string" VARCHAR
  >,
  "tracey-id" STRUCT <
    "propertyType" VARCHAR,
    "boolean" BOOLEAN,
    "byte" VARCHAR,
    "short" INTEGER,
    "integer" INTEGER,
    "long" BIGINT,
    "float" DOUBLE,
    "double" DOUBLE,
    "string" VARCHAR
  >,
  "JMSXGroupID" STRUCT <
    "propertyType" VARCHAR,
    "boolean" BOOLEAN,
    "byte" VARCHAR,
    "short" INTEGER,
    "integer" INTEGER,
    "long" BIGINT,
    "float" DOUBLE,
    "double" DOUBLE,
    "string" VARCHAR
  >,
  "JMSXGroupSeq" STRUCT <
    "propertyType" VARCHAR,
    "boolean" BOOLEAN,
    "byte" VARCHAR,
    "short" INTEGER,
    "integer" INTEGER,
    "long" BIGINT,
    "float" DOUBLE,
    "double" DOUBLE,
    "string" VARCHAR
  >
>)
WITH (KAFKA_TOPIC='incoming.activemq', VALUE_FORMAT='JSON');