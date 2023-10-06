# FIWARE Data Space Connector

The FIWARE Data Space Connector is an integrated suite of components implementing DSBA Technical Convergence recommendations, every organization participating 
in a data space should deploy to “connect” to a data space.

This repository provides the charts and deployment recipes. 

A more extensive documentation about the connector and the supported flows in a data space it supports can be found at the 
FIWARE [data-space-connector repository](https://github.com/FIWARE/data-space-connector).



## Deployment


### Deployment with ArgoCD

The FIWARE Data Space Connector is a [Helm](https://helm.sh) chart using a gitops-approach, following 
the [app-of-apps pattern](https://argo-cd.readthedocs.io/en/stable/operator-manual/cluster-bootstrapping), with [ArgoCD](https://argo-cd.readthedocs.io/en/stable/). 

This repository already provides a [deployment Github action](.github/workflows/deploy.yaml) compatible with OpenShift clusters, performing deployments out of 
a branch created in the format `deploy/<TARGET_NAMESPACE>` and pulling the `values.yaml` from a specified gitops repository. It also requires to set the 
following ENVs for the Github action, `OPENSHIFT_SERVER` and `OPENSHIFT_TOKEN`, specifying the OpenShift target URL and access token, respectively.  
For deployment, simply fork this repository, adapt the configuration of the action to your setup and set the necessary ENVs. After creating a 
`deploy/<TARGET_NAMESPACE>` branch, it will perform the deployment to the specified namespace.

For a different cluster flavor, the GitHub action needs to be modified before to be compatible.


### Deployment with Helm

Even though a gitops-approach, following the app-of-apps pattern, with ArgoCD, is the preferred way to deploy the Data-Space-Connector, not everyone has it available. Therefore, the Data-Space-Connector is also provided as an [Umbrella-Chart](https://helm.sh/docs/howto/charts_tips_and_tricks/#complex-charts-with-many-dependencies), containing all the sub-charts and their dependencies.

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

The chart also contains the [argo-cd applications support](./data-space-connector/templates/), thus it can be used to generate argo-deployments, too. In plain Helm deployments, this should be disabled in the values.yaml:
```yaml
argoApplications: false
```

Configurations for all sub-charts(and sub-dependencies) can be managed through the top-level values.yaml of the chart. It contains the default values of each [application](./applications/). The configuration of the applications can be changed under the key ```<APPLICATION_NAME>```, please see the individual applications and there sub-charts for the available options. 
Example:
In order to change the image-tag of [Keycloak](./applications/keycloak/) and the issuer did used by it, the values.yaml looks as following:
```yaml
keycloak:
    # configuration directly in the application chart, extending the original keycloak chart
    didConfig:
        domain: "my-new-did-domain.org"
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
under [./examples](./examples).
