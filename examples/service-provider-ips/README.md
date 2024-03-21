# Service Provider - IPS

Example deployment of a packet delivery service provider named IPS. 


## Prerequisites

Assuming existing namespace `ips`, where the connector will be deployed. 

Assuming [nginx-ingress](https://docs.nginx.com/nginx-ingress-controller/) as Ingress Controller 
and [cert-manager](https://cert-manager.io/) being configured to issue certificates 
for domain `*.aws.fiware.io` with ClusterIssuer `letsencrypt-fiware-eks`.  
When using a different Ingress Controller or specific load balancer, make sure to add 
the necessary annotations.  
Also change the domains and hostnames according to your DNS config.

It is assumed, that the organisation IPS is part of a data space where the trusted participant list 
can be found at [https://tir.dsba.fiware.dev](https://tir.dsba.fiware.dev).  
When operating a different data space with different trusted participant list, change this 
accordingly.


## Deployment with helm

After downloading the chart (see [../../README.md#deployment-with-helm](../../README.md#deployment-with-helm)), 
use the following command:
```shell
helm install -n ips -f ./values-dsc.yaml ips-dsc <PATH-TO-DSC>/data-space-connector/charts/data-space-connector
```

Alternatively, install using the remote chart:
```shell
helm repo add dsc https://fiware-ops.github.io/data-space-connector/
helm install -n ips -f ./values-dsc.yaml ips-dsc dsc/data-space-connector
```

## Authentication at IPS

For authentication VCs of type [`InternationalParcelService`](https://github.com/FIWARE-Ops/fiware-gitops/blob/5698dedd9e75620c5706841b06da357cb0b1096a/aws/dsba/happypets/walt-id/values.yaml#L88) need to be issued. Make sure that the consumer issuer is configured 
for such credential type (e.g., adding a [client](https://github.com/FIWARE-Ops/fiware-gitops/blob/5698dedd9e75620c5706841b06da357cb0b1096a/aws/dsba/happypets/keycloak/templates/realmConfigMap.yaml#L315) in the Keycloak realm of the consumer issuer). 

The IPS `credentials-config-service` (CCS) requires an entry for such credentials, pointing to the IPS `trusted-issuers-list` 
and data space `trusted-issuers-registry` at [https://tir.dsba.fiware.dev](https://tir.dsba.fiware.dev).  

Below is an example when using curl to add an entry at the CCS:
```shell
curl -X 'POST' \
'http://ips-dsc-credentials-config-service:8080/service' \
-H 'Accept: */*' \
-H 'Content-Type: application/json' \
-d '{
      "id": "ips-service",
      "defaultOidcScope": "default",
      "oidcScopes": {
		  "default": [
			  {
				  "type": "InternationalParcelService",
				  "trustedParticipantsLists": [
					  "https://tir.dsba.fiware.dev"
				  ],
				  "trustedIssuersLists": [
					  "http://ips-dsc-trusted-issuers-list:8080"
				  ]
			  }
		  ]
	  }
    }'
```

Alternatively, some application charts allow to create initial entries during deployment, e.g., compare to 
the [portal](https://github.com/FIWARE-Ops/fiware-gitops/blob/5698dedd9e75620c5706841b06da357cb0b1096a/aws/dsba/packet-delivery/portal/values.yaml#L27) 
of the Packet Delivery example.
