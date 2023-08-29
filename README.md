# FIWARE Data Space Connector

Connector bundling all components

## Deployment with Helm

Even thought a gitops-approach, following the app-of-apps pattern, with [ArgoCD](https://argo-cd.readthedocs.io/en/stable/), is the preferred way to deploy the Data-Space-Connector, not everyone has has it available. Therefor, the Data-Space-Connector is also provided as an [Umbrella-Chart](https://helm.sh/docs/howto/charts_tips_and_tricks/#complex-charts-with-many-dependencies), containing all the sub-charts and their dependencies.

The chart is available at the repository ```https://fiware-ops.github.io/data-space-connector/```. You can install it via:

```shell
    # add the repo
    helm repo add dsc https://fiware-ops.github.io/data-space-connector/
    # install the chart
    helm install dsc/data-space-connector
```

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
