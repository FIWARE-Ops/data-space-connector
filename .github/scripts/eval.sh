#! /bin/bash

CHARTS=$(pwd)/charts/*
RETURN_VAL=0
for chart in $CHARTS
do
 ./bin/helm dependency build ${chart}
 ./bin/helm template ${chart} | kubeconform -strict

 ret=$?
 if [ $ret -ne 0 ]; then
     RETURN_VAL=$ret
 fi
done

exit $RETURN_VAL
