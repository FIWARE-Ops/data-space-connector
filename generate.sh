# move to a charts folder
mkdir charts
cp -r data-space-connector charts/

dscChartFolder=charts/data-space-connector
cp -R applications/. $dscChartFolder/charts/



for f in $(find $dscChartFolder/charts -name 'Chart.yaml'); do
    name=$(yq eval .name $f);
    version=$(yq eval .version $f);
    yq eval -i '.dependencies += {"name":"'$name'","version":"'$version'", "repository":"file://./charts/'$name'", "condition":"'$name'.deploymentEnabled"}'  $dscChartFolder/Chart.yaml
done
echo \ >>  $dscChartFolder/values.yaml
echo \ >>  $dscChartFolder/values.yaml
echo '#Sub-Chart configuration configuration' >>  $dscChartFolder/values.yaml
for dir in $dscChartFolder/charts/*/; do
    chartsFile=${dir}Chart.yaml
    valuesFile=${dir}values.yaml
    name=$(yq e '.name' $chartsFile)
    
    echo \ >>  $dscChartFolder/values.yaml
    echo $name: >>  $dscChartFolder/values.yaml
    echo "  # Enable the deployment of application: $name" >>  $dscChartFolder/values.yaml
    echo "  deploymentEnabled: true" >>  $dscChartFolder/values.yaml
    echo \ >>  $dscChartFolder/values.yaml
    cat $valuesFile | sed 's/^/  /' >>  $dscChartFolder/values.yaml

    helm dependency build ${dir}
done
# fix values in the chart yaml
version="${1:=0.0.0}"
yq e -i '.version = "'$version'"'  $dscChartFolder/Chart.yaml

