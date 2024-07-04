#! /bin/bash

CHARTS=$(pwd)/charts/*
for chart in $CHARTS
do
 ./bin/helm template ${chart} | kubeconform -strict
done
