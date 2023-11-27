import { Construct } from "constructs";
import { azlist, scorpiobroker_sqs_object } from "../constants"
import { Aws, CfnOutput, CustomResource, Duration, Names, Stack } from "aws-cdk-lib";
import { Code, Runtime, Function, Architecture } from "aws-cdk-lib/aws-lambda";
import { PolicyStatement } from "aws-cdk-lib/aws-iam";
import { Provider } from "aws-cdk-lib/custom-resources";
import { Parameters } from "../../../../parameters";

export interface GarnetUtilProps {}

export class Utils extends Construct { 

    public readonly az1: string
    public readonly az2: string

    constructor(scope: Construct, id: string, props?: GarnetUtilProps) {
        super(scope, id)
        
        if(Stack.of(this).region.startsWith('$')){
          throw new Error('Please type a valid region in the parameter.ts file')
        }

        if(!azlist[`${Stack.of(this).region}`]){
            throw new Error('The stack is not yet available in the region selected')
          }
      
          const compatible_azs = azlist[`${Stack.of(this).region}`]
      
          const get_az_func_path = `${__dirname}/lambda/getAzs`
          const get_az_func = new Function(this, 'AzFunction', {
              functionName: `garnet-utils-az-lambda-${Names.uniqueId(this).slice(-8).toLowerCase()}`,
               runtime: Runtime.NODEJS_18_X,
               code: Code.fromAsset(get_az_func_path),
               handler: 'index.handler',
               timeout: Duration.seconds(50),
               architecture: Architecture.ARM_64,
               environment: {
                  COMPATIBLE_AZS: JSON.stringify(compatible_azs)
                }
          })
          get_az_func.addToRolePolicy(new PolicyStatement({
            actions: ["ec2:DescribeAvailabilityZones"],
            resources: ['*'] 
           }))
        
           const get_az_provider = new Provider(this, 'getAzCleanUpprovider', {
            onEventHandler: get_az_func
          }) 
          
          const get_az = new CustomResource(this, 'getAzCustomResource', {
            serviceToken: get_az_provider.serviceToken
          })

          this.az1 = get_az.getAtt('az1').toString()
          this.az2 = get_az.getAtt('az2').toString()
       
          
          if(Parameters.garnet_broker == 'Scorpio'){

            let sqs_urls = Object.values(scorpiobroker_sqs_object).map(q => `https://sqs.${Aws.REGION}.amazonaws.com/${Aws.ACCOUNT_ID}/${q}`)

            const scorpio_sqs_lambda_path = `${__dirname}/lambda/scorpioSqs`
            const scorpio_sqs_lambda = new Function(this, 'ScorpioSqsFunction', {
              functionName: `garnet-utils-scorpio-sqs-lambda-${Names.uniqueId(this).slice(-8).toLowerCase()}`,
               runtime: Runtime.NODEJS_18_X,
               code: Code.fromAsset(scorpio_sqs_lambda_path),
               handler: 'index.handler',
               timeout: Duration.seconds(50),
               architecture: Architecture.ARM_64,
               environment: {
                  SQS_QUEUES: JSON.stringify(sqs_urls)
                }
          })
          scorpio_sqs_lambda.addToRolePolicy(new PolicyStatement({
            actions: ["sqs:DeleteQueue"],
            resources: [`arn:aws:sqs:${Aws.REGION}:${Aws.ACCOUNT_ID}:garnet-scorpiobroker-*`] 
           }))
        
           const scorpio_sqs_provider = new Provider(this, 'scorpioSqsProvider', {
            onEventHandler: scorpio_sqs_lambda
          }) 
          
          const scorpio_sqs_resource = new CustomResource(this, 'scorpioSqsCustomResource', {
            serviceToken: scorpio_sqs_provider.serviceToken
          })

          }
      
    }
}
