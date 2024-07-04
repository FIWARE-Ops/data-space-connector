#! /bin/bash

CHARTS=$(pwd)/charts/*
for chart in $CHARTS
do
 ./bin/helm dependency build ${chart}
 ./bin/helm template ${chart} | kubeconform -strict
done
