# Examples

Different examples for the deployment of the FIWARE Data Space Connector, as well as the integration with 
other frameworks.

<details>
<summary><strong>Contents</strong></summary>

- [Deployment of service providers](#deployment-of-service-providers)
  - [IPS Service Provider (helm)](#ips-service-provider-helm)
  - [Packet Delivery Company (ArgoCD)](#packet-delivery-company-argocd)
- [Integration with AWS Smart Territory Framework](#integration-with-aws-smart-territory-framework)

</details>


## Deployment of service providers

### IPS Service Provider (helm)

This is an example of a data service provider, providing a fictitious digital service 
for packet delivery services as a company called `IPS`. 

The service is provided by the orion-ld ontext Broker via the NGSI-LD API, offering 
access to the entities of certain delivery orders.

The example uses plain helm for the deployment.

More information can be found here:
* [./service-provider-ips](./service-provider-ips)



### Packet Delivery Company (ArgoCD)

This is an example of a data service provider called Packet Delivery Company (PDC).

Basically, it's identical to IPS above, but deployment is performed via 
[GitOps pattern](https://www.gitops.tech/) and [ArgoCD](https://argo-cd.readthedocs.io/en/stable/).

The configuration can be found at the 
[fiware-gitops repository](https://github.com/FIWARE-Ops/fiware-gitops/tree/master/aws/dsba/packet-delivery/data-space-connector).




## Integration with AWS Smart Territory Framework

This is an example of a data service provider that is integrated with the 
[AWS Smart Territory Framework (STF)](https://github.com/aws-samples/aws-stf). 

In general, this example deploys a data service provider based on the Data Space Connector, 
but integrating the FIWARE Context Broker from the STF.

More information can be found here:
* [./aws-smart-territory-framework](./aws-smart-territory-framework)

