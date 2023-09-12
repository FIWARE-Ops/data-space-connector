#!/bin/bash

NAMESPACE="ips"
PODS=$(kubectl get pods -n $NAMESPACE --no-headers -o custom-columns=":metadata.name")

for POD in $PODS; do
  echo "Logs for pod: $POD"
  logs=$(kubectl logs -n $NAMESPACE $POD --tail=10)
  if [[ -z "$logs" ]]; then
    echo "No logs found for pod $POD"
  else
    echo "$logs" > "./podLogs/$POD.log"
    echo "----------------------------------" >> "./podLogs/$POD.log"
  fi
done