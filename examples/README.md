# Examples

Different examples for the deployment of the FIWARE Data Space Connector


## IPS Service Provider (helm)

This is an example of a data service provider, providing a fictitious digital service 
for packet delivery services as a company called `IPS`. 

The service is provided by the orion-ld ontext Broker via the NGSI-LD API, offering 
access to the entities of certain delivery orders.

The example uses plain helm for the deployment.

* [./service-provider-ips](./service-provider-ips)



## Packet Delivery Company (ArgoCD)

This is an example of a data service provider called Packet Delivery Company (PDC).

Basically, it's identical to IPS above, but deployment is performed via 
[GitOps pattern](https://www.gitops.tech/) and [ArgoCD](https://argo-cd.readthedocs.io/en/stable/).

The configuration can be found at the 
[fiware-gitops repository](https://github.com/FIWARE-Ops/fiware-gitops/tree/master/aws/dsba/packet-delivery/data-space-connector).
