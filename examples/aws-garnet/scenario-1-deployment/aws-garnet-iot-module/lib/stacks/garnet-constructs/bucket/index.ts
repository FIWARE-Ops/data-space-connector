import { CustomResource, Duration, Names } from "aws-cdk-lib";
import { Runtime, Function, Code, Architecture } from "aws-cdk-lib/aws-lambda";
import { Construct } from "constructs";
import { Parameters } from "../../../../parameters";
import { PolicyStatement } from "aws-cdk-lib/aws-iam";
import { Provider } from "aws-cdk-lib/custom-resources";

export interface GarnetBucketProps {

  }


export class GarnetBucket extends Construct {

    public readonly bucket_name: string

    constructor(scope: Construct, id: string, props: GarnetBucketProps) {
        super(scope, id)

        const lambda_bucket_path = `${__dirname}/lambda/bucketHead`
        const lambda_bucket = new Function(this, 'AzFunction', {
             functionName: `garnet-custom-bucket-${Names.uniqueId(this).slice(-8).toLowerCase()}`,
             runtime: Runtime.NODEJS_18_X,
             code: Code.fromAsset(lambda_bucket_path),
             handler: 'index.handler',
             timeout: Duration.seconds(50),
             architecture: Architecture.ARM_64,
             environment: {
                BUCKET_NAME: Parameters.garnet_bucket
              }
        })

        lambda_bucket.addToRolePolicy(new PolicyStatement({
            actions: ["s3:CreateBucket"],
            resources: ["arn:aws:s3:::*"] 
           }))

        const bucket_provider = new Provider(this, 'CustomBucketProvider', {
          onEventHandler: lambda_bucket
        }) 

       new CustomResource(this, 'CustomBucketProviderResource', {
            serviceToken: bucket_provider.serviceToken
        })


        this.bucket_name = Parameters.garnet_bucket 

    }
}