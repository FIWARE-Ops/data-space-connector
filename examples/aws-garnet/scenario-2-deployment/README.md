# Integration with AWS Garnet Framework

## 2/ Existing AWS Garnet Framework deployment in the AWS Account with a Context Broker on AWS ECS Fargate
For this scenario, it is recommended that a modified version of the Helm Chart for the Data Spaces Connector is deployed to a Kubernetes Cluster in the service Amazon Elastic Kubernetes Service ([AWS EKS](https://aws.amazon.com/eks/)).
In this case, considering that your environment for the AWS Garnet Framework was set up following the [official AWS GitHub Repository](https://github.com/aws-samples/aws-stf-core), the FIWARE Orion-LD Context Broker is already hosted as an Amazon Elastic Container Service ([AWS ECS](https://aws.amazon.com/ecs/)) task in an AWS Fargate cluster and the integration to the Data Spaces Connector will be performed by deploying only this modified Helm Chart available [in this reference](./yaml/values-dsc-awl-load-balancer-controller-scenario2.yaml).

<br>

![Target Architecture for extending the deployment of an existing AWS Garnet Framework](../static-assets/garnet-ds-connector-scenario2.png)

<br> 

### IPS Service Provider Deployment in Amazon EKS 
This section covers the setup of the prerequisites of the IPS Service Provider examples of this repository, available in [this reference](../service-provider-ips/README.md).

#### Changes to the original Helm chart 
[The edited version of the IPS Service Provider example Helm Chart](./yaml/values-dsc-awl-load-balancer-controller-scenario2.yaml) contains 3 main differences for this scenario where an existing Context Broker is already deployed and must only by extended by the additional building blocks of the Data Spaces Connector:

* Disable the deployment of the MongoDB database

```shell
mongodb:
  # Disable the deployment of application: mongodb
  deploymentEnabled: false
```

* Disable the deployment of the Orion-LD Context Broker

```shell
orion-ld:
  # Disable the deployment of application: orion-ld
  deploymentEnabled: true
```

* Replace the host for the Kong proxy to point it to AWS Garnet Framework's Unified API based on API Gateway. The value for the host parameter can be found in your [AWS Cloud Formation](https://console.aws.amazon.com/cloudformation/home) Stack named `Garnet` > Outputs tab > `GarnetEndpoint` > Value.

```shell
    # Provide the kong.yml configuration (either as existing CM, secret or directly in the values.yaml)
    dblessConfig:
      configMap: ""
      secret: ""
      config: |
        _format_version: "2.1"
        _transform: true

        consumers:
        - username: token-consumer
          keyauth_credentials:
          - tags:
            - token-key
            - tir-key
            
    #TODO - Replace here with the AWS Garnet Framework Unified API endpoint 
        services:
          - host: "https://xxxxxxxxxx.execute-api.eu-west-1.amazonaws.com" 
            name: "ips"
            port: 1026
            protocol: http
```

#### Helm Chart install steps

* IPS Kubernetes namespace creation 

```shell
kubectl create namespace ips
```

* Add FIWARE Data Space Connector Remote repository

```shell
helm repo add dsc https://fiware-ops.github.io/data-space-connector/
```

* Install the Helm Chart using the provided file `./yaml/values-dsc-awl-load-balancer-controller-scenario2.yaml`

```shell
helm install -n ips -f ./yaml/values-dsc-awl-load-balancer-controller-scenario2.yaml ips-dsc dsc/data-space-connector
```