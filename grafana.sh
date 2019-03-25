#!/usr/bin/env bash

curl -s -X "POST" "http://grafana:3000/api/datasources" -H "Content-Type: application/json" \
--user admin:admin \
-d $'{"id":1,"orgId":1,"name":"DS_AUDIT","type":"elasticsearch","typeLogoUrl":"public/app/plugins/datasource/elasticsearch/img/elasticsearch.svg","access":"proxy","url":"http://elasticsearch:9200","password":"","user":"","database":"audit","basicAuth":false,"isDefault":false,"jsonData":{"timeField":"TIMESTAMP"}}'
