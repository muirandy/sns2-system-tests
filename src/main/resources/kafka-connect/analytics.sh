#!/usr/bin/env bash

connectServer=${1:-localhost:8083/connectors}
elasticSearchInternalNetworkUrl=${2:-elasticsearch:9200}
echo " --- analytics.sh --- "
echo $connectServer
echo $elasticSearchInternalNetworkUrl

curl -X PUT "elasticsearch:9200/_template/template_1" -H 'Content-Type: application/json' -d'
{
  "index_patterns": [
    "audit"
  ],
  "settings": {
    "number_of_shards": 5
  },
  "mappings": {
    "kafka-connect": {
      "properties": {
        "ASSOCIATION": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "EVENT": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "ORDER_ID": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "TIMESTAMP": {
          "type": "date",
          "format": "yyyy-MM-dd HH:mm:ss.SSS",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        }
      }
    }
  }
}
'

payload='{
 "name": "audit-es-sink",
  "config": {
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "tasks.max": "1",
    "topics": "AUDIT,modify.op.msgs,RAW_VOIP_INSTRUCTIONS,enriched.modification.instructions.with.dn,SINK_MODIFY_VOIP_INSTRUCTIONS_WITH_SWITCH_ID",
    "key.ignore": "true",
    "topic.schema.ignore":"true",
    "schema.ignore":"true",
    "connection.url": "http://'
payload+=$elasticSearchInternalNetworkUrl
payload+='",
    "type.name": "kafka-connect",
    "name": "audit-es-sink"
  }
}'

echo "Analytics payload:"
echo $payload

curl -X POST -H "Content-Type: application/json" \
http://$connectServer \
  --data "$payload"
