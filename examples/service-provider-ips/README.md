# Service Provider - IPS

Example deployment of a packet delivery service provider named IPS. 


## Deployment with helm

Assuming existing namespace `ips`. 

Assuming nginx-ingress as Ingress Controller and cert-manager being configured to issue certificates 
for domain `*.aws.fiware.io` with ClusterIssuer `letsencrypt-fiware-eks`.  
When using a different Ingress Controller or specific load balancer, make sure to add 
the necessary annotations.

It is assumed, that the organisation IPS is part of a data space where the trusted participant list 
can be found at [https://tir.dsba.fiware.dev](https://tir.dsba.fiware.dev). 

After downloading the chart (see [../../README.md#deployment-with-helm](../../README.md#deployment-with-helm)), 
use the following command:
```shell
helm install -n ips -f ./values-dsc.yaml ips-dsc <PATH-TO-DSC>/data-space-connector/charts/data-space-connector
```

Or install using the remote chart:
```shell
helm repo add dsc https://fiware-ops.github.io/data-space-connector/
helm install -n ips -f ./values-dsc.yaml ips-dsc dsc/data-space-connector
```

