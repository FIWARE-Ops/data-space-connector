# Integration with AWS Garnet Framework

## 1/ No existing AWS Garnet Framework deployment in the AWS Account
For this scenario, it is recommended that the complete Helm Chart for the Data Spaces Connector is deployed to a Kubernetes Cluster in the service Amazon Elastic Kubernetes Service ([AWS EKS](https://aws.amazon.com/eks/)).
In this case, the FIWARE Context Broker will be hosted by a pod in the Kubernetes cluster and the integration to the AWS Garnet Framework will be performed by deploying only the [AWS Garnet IoT module](https://github.com/aws-samples/aws-stf-core#stf-iot) of the framework. Further configuration will include streamlining the Garnet IoT pipeline to the Internal Service Load Balancer associated to the EKS cluster.

<br>

![Target Architecture for a fresh deployment of AWS Garnet Framework with the DS Connector](../static-assets/garnet-ds-connector-scenario1.png)

<br>

### IPS Service Provider Deployment in Amazon EKS 
This section covers the setup of the prerequisites of the IPS Service Provider examples of this repository, available in [this reference](../service-provider-ips/README.md).

* IPS Kubernetes namespace creation 

```shell
kubectl create namespace ips
```

* Add FIWARE Data Space Connector Remote repository

```shell
helm repo add dsc https://fiware-ops.github.io/data-space-connector/
```

* Install the Helm Chart using the provided file `./yaml/values-dsc-awl-load-balancer-controller.yaml`

```shell
helm install -n ips -f ./yaml/values-dsc-awl-load-balancer-controller.yaml ips-dsc dsc/data-space-connector
```

### Deployment of AWS Garnet Framework IoT Module
An AWS CDK project modified from the AWS Garnet Framwork main project is available in [this repository](./aws-garnet-iot-module/). The project was modified so ONLY the AWS Garnet Framwrork IoT Module is deployed once the CDK stacks are deployed. To integrate this module to the Context Broker deployed in the Amazon EKS Cluster, 2 main parameters must be set in the `./aws-garnet-iot-module/parameters.ts` file :

```shell
    // FIWARE DATA SPACE CONNECTOR PARAMETERS
    amazon_eks_cluster_load_balancer_dns: "",
    amazon_eks_cluster_load_balancer_listener_arn: "",
```

Edit the file including the respective strings referencing your Load Balancer resource for the Data Space Connector deployed in the Amazon EKS Cluster in your AWS Account.

Then, deploy the CDK stack using the following commands:

```shell
cd aws-garnet-iot-module
```

```shell
npm install
```

```shell
cdk bootstrap
```

```shell
cdk deploy
```