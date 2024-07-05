# FIWARE Data Space Connector

The FIWARE Data Space Connector is an integrated suite of components implementing DSBA Technical Convergence recommendations, every organization participating 
in a data space should deploy to “connect” to a data space.

This repository provides the charts and deployment recipes. 

A more extensive documentation about the connector and the supported flows in a data space it supports can be found at the 
FIWARE [data-space-connector repository](https://github.com/FIWARE/data-space-connector).



## Deployment

### Local Deployment

The FIWARE Data Space Connector provides a local deployment of a Minimal Viable Dataspace. 
Find a detailed documentation here: [Local Deployment](./doc/LOCAL.MD)

### Deployment with Helm

The Data-Space-Connector is a [Helm Umbrella-Chart](https://helm.sh/docs/howto/charts_tips_and_tricks/#complex-charts-with-many-dependencies), containing all the sub-charts of the different components and their dependencies.

The chart is available at the repository ```https://fiware-ops.github.io/data-space-connector/```. You can install it via:

```shell
    # add the repo
    helm repo add dsc https://fiware-ops.github.io/data-space-connector/
    # install the chart
    helm install <DeploymentName> dsc/data-space-connector -n <Namespace> -f values.yaml
```
**Note,** that due to the app-of-apps structure of the connector and the different dependencies between the components, a deployment without providing any configuration values will not work. Make sure to provide a 
`values.yaml` file for the deployment, specifying all necessary parameters. This includes setting parameters of the connected data space (e.g., trust anchor endpoints), DNS information (providing Ingress or OpenShift Route parameters), 
structure and type of the required VCs, internal hostnames of the different connector components and providing the configuration of the DID and keys/certs.  
Also have a look at the [examples](#examples).

Configurations for all sub-charts (and sub-dependencies) can be managed through the top-level [values.yaml](./charts/data-space-connector/values.yaml) of the chart. It contains the default values of each component and additional parameter shared between the components. The configuration of the applications can be changed under the key ```<APPLICATION_NAME>```, please see the individual applications and there sub-charts for the available options.  
Example:
In order to change the image-tag of [Keycloak](./argocd/applications/keycloak/), the values.yaml looks as following:
```yaml
keycloak:
    # configuration for the keycloak-sub-chart. Its used as a dependency to the application, thus all config is accessible under the dependency name
    keycloak:
        image:
            tag: LATEST_GREATEST
```

The chart is [generated](generate.sh) on each merge to master from the current app-of-apps, thus no source is constantly available. For details, unpack the chart directly and check its contents:
```shell  
    # set version you are interested in
    version=0.0.1
    wget https://github.com/FIWARE-Ops/data-space-connector/releases/download/data-space-connector-$version/data-space-connector-$version.tgz | && tar -xzvf data-space-connector-$version.tgz
```


### Examples

Different examples for the deployment of the FIWARE Data Space connector can be found 
under the [./examples](./examples) directory.

## Testing

In order to test the [helm-charts](./charts/) provided for the FIWARE Data Space Connector, an integration-test framework based on [Cucumber](https://cucumber.io/) and [Junit5](https://junit.org/junit5/) is provided: [it](./it).

The tests can be executed via: 
```shell
    mvn clean integration-test -Ptest
```
They will spin up the [Local Data Space](./doc/LOCAL.MD) and run the [test-scenarios](./it/src/test/resources/it/mvds_basic.feature) against it.


## Additional Resources

Following is a list with additional resources about the FIWARE Data Space Connector and Data Spaces in general:
* [FIWARE Webinar about Data Spaces, its roles and components (by Stefan Wiedemann)](https://www.youtube.com/watch?v=hm5qMlhpK0g)

