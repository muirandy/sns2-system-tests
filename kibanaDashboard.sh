#!/usr/bin/env bash
if [[ -z "${KIBANA_HOST}" ]]; then
  echo -e "KIBANA_HOST not set, defaulting to kibana"
  KIBANA_HOST="kibana"
fi

echo "Loading Kibana ClickStream Dashboard"

curl -s -X "POST" "http://$KIBANA_HOST:5601/api/kibana/dashboards/import" \
        -H "kbn-xsrf: true" \
	    -H "Content-Type: application/json" \
	     --data-binary @kibanaDashboard.json

echo ""
echo ""

echo -e "Navigate to:\n\thttp://kibana:5601/app/kibana#/dashboard/feb4ba90-4f17-11e9-901a-0dbd9d57936b"