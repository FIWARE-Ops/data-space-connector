# Service Provider - IPS

Example deployment of a packet delivery service provider named IPS. 


## Deployment with helm

Assuming existing namespace `ips`. 

After downloading the chart, use the following command:
```shell
helm install -n ips -f ./values-dsc.yaml ips-dsc <PATH-TO-DSC>/data-space-connector/charts/data-space-connector
```

Or install using the remote chart:
```shell
helm repo add dsc https://fiware-ops.github.io/data-space-connector/
helm install -n ips -f ./values-dsc.yaml ips-dsc dsc/data-space-connector
```

