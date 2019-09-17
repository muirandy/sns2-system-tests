#!/usr/bin/env bash
#curl -X "GET" "http://localhost:3000/api/dashboards/db/click-stream-analysis" \
#        -H "Content-Type: application/json" \
#	     --user admin:admin

echo " --- grafanaDashboard.sh --- "

if [[ -z "${GRAFANA_HOST}" ]]; then
  echo -e "GRAFANA_HOST not set, defaulting to grafana"
  GRAFANA_HOST="grafana"
fi

echo "Loading Grafana ClickStream Dashboard"

RESP="$(curl -s -X "POST" "http://$GRAFANA_HOST:3000/api/dashboards/db" \
	    -H "Content-Type: application/json" \
	     --user admin:admin \
	     --data-binary @grafanaDashboard.json)"

#echo $RESP
echo ""
echo ""

if [[ $RESP =~ .*\"url\":\"([^\"]*)\".* ]]
then
    url="${BASH_REMATCH[1]}"
else
    url="/dashboard/db/audit-dashboard"
fi

echo -e "Navigate to:\n\thttp://grafana:3000${url}\n(Default user: admin / password: admin)"