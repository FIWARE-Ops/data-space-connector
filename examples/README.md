# Examples

Different examples for the deployment of the FIWARE Data Space Connector, as well as the integration with 
other frameworks.

<details>
<summary><strong>Contents</strong></summary>

- [Deployment of service providers](#deployment-of-service-providers)
  - [Local deployment of Minimal Viable Dataspace (helm/k3s)](#local)
  - [Packet Delivery Company (ArgoCD)](#packet-delivery-company-argocd)
- [Integration with AWS Garnet Framework](#integration-with-aws-garnet-framework-formerly-aws-smart-territory-framework)

</details>



## Deployment of service providers

### Local deployment of Minimal Viable Dataspace (helm/k3s)

This is an example of a "Minimal Viable Dataspace", consisting of a fictitious data service 
provider called M&P Operations Inc. (using the FIWARE Data Space Connector), a data service consumer 
called Fancy Marketplace Co. and the 
data space's trust anchor.

The service is provided by the Scorpio Context via the NGSI-LD API, offering access to 
energy report entities.

The example uses [k3s](https://k3s.io/) and helm for deployment.

More information can be found here:
* [Local Deployment](../doc/LOCAL.md)



### Packet Delivery Company (ArgoCD)

This is an example of a data service provider called Packet Delivery Company (PDC).

The deployment is performed via 
[GitOps pattern](https://www.gitops.tech/) and [ArgoCD](https://argo-cd.readthedocs.io/en/stable/).

The configuration can be found at the 
[fiware-gitops repository](https://github.com/FIWARE-Ops/fiware-gitops/tree/master/aws/dsba/packet-delivery/data-space-connector).




## Integration with AWS Garnet Framework (formerly AWS Smart Territory Framework)

This is an example of a data service provider that is integrated with the 
[AWS Garnet Framwork (formerly AWS STF)](https://github.com/aws-samples/aws-stf). 

In general, this example deploys a data service provider based on the Data Space Connector, 
but integrating the FIWARE Context Broker from the STF.

More information can be found here:
* [./aws-garnet](./aws-garnet)

