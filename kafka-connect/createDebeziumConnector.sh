#!/bin/sh

curl -i -X POST -H "Accept:application/json" \
    -H  "Content-Type:application/json" \
    http://localhost:8083/connectors \
    -d '{
      "name": "netstream-source-debezium-xstream",
      "config": {
            "connect.meta.data":"false",
            "connector.class":"io.debezium.connector.oracle.OracleConnector",
            "database.server.name":"netstream",
            "database.hostname":"faithDB",
            "database.port":"1521",
            "database.user":"c##xstrm",
            "database.password":"xs",
            "database.dbname":"db1",
            "database.out.server.name":"dbzxout",
            "database.history.kafka.bootstrap.servers" : "kafka:9092",
            "database.history.kafka.topic":"schema-changes.inventory",
            "database.tablename.case.insensitive": "true",
            "database.oracle.version":"11",
            "auto.register.schemas":"false",
            "include.schema.changes":"true",
            "table.whitelist":"db1.service_owner.services",
            "transforms":"InsertTopic,InsertSourceDetails",
            "transforms.Cast.type":"com.aimyourtechnology.kafka.connect.transform.bytes.Cast",
            "transforms.Cast.spec":"before.SERVICE_ID.value:string,before.ENDPOINT_ID.value:string,before.PRODUCT_SPEC_ID.value:string,before.PROVIDER_ID.value:string",
            "transforms.Flatten.type":"org.apache.kafka.connect.transforms.Flatten$Value",
            "transforms.flatten.delimiter":"_",
            "transforms.ConvertBytes.type":"org.apache.kafka.connect.transforms.Cast$Value",
            "transforms.ConvertBytes.spec":"after->SERVICE_ID:int32",
            "transforms.InsertTopic.type":"org.apache.kafka.connect.transforms.InsertField$Value",
            "transforms.InsertTopic.topic.field":"messagetopic",
            "transforms.InsertSourceDetails.type":"org.apache.kafka.connect.transforms.InsertField$Value",
            "transforms.InsertSourceDetails.static.field":"messagesource",
            "transforms.InsertSourceDetails.static.value":"Debezium CDC from Oracle on netstream",
            "key.converter": "io.confluent.connect.avro.AvroConverter",
            "key.converter.schema.registry.url": "http://schema-registry:8081",
            "value.converter": "io.confluent.connect.avro.AvroConverter",
            "value.converter.schema.registry.url": "http://schema-registry:8081"
       }
    }'

            "transforms":"Cast,InsertTopic,InsertSourceDetails",

            "transforms.Flatten.type":"org.apache.kafka.connect.transforms.Flatten$Value",
            "transforms": "InsertTopic,InsertSourceDetails",

            "transforms": "InsertTopic,InsertSourceDetails,ConvertBytes",
            "transforms.ConvertBytes.type":"org.apache.kafka.connect.transforms.Cast$Value",
            "transforms.ConvertBytes.spec":"after->SERVICE_ID-:string",

            "transforms": "Bytes",
            "transforms.Bytes.type": "com.aimyourtechnology.kafka.connect.transform.bytes.BytesTransformation"
            "table.whitelist": "service_owner\\.services",
            "table.whitelist": "service_owner.services",
            "table.whitelist":"orcl\\.debezium\\.(.*)",

#create stream        with (kafka_topic='netstream.SERVICE_OWNER.SERVICES', value_format='AVRO');