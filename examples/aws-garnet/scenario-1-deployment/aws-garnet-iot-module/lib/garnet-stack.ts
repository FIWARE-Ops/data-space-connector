import { CfnElement, CfnOutput, Names, Stack, StackProps } from 'aws-cdk-lib'
import { Construct } from 'constructs'
//import { GarnetScorpio } from './stacks/garnet-scorpio/garnet-scorpio'
import { GarnetIotStack } from './stacks/garnet-iot/garnet-iot-stack'
import { Parameters } from '../parameters'
import { GarnetOrion } from './stacks/garnet-orion/garnet-orion'
import { GarnetConstructs } from './stacks/garnet-constructs/garnet-constructs'
import { GarnetPrivateSub } from './stacks/garnet-constructs/privatesub'


export class GarnetStack extends Stack {


  getLogicalId(element: CfnElement): string {
    if (element.node.id.includes('NestedStackResource')) {
        return /([a-zA-Z0-9]+)\.NestedStackResource/.exec(element.node.id)![1] // will be the exact id of the stack
    }
    return super.getLogicalId(element)
}


  constructor(scope: Construct, id: string, props?: StackProps) {
    super(scope, id, props)

    const garnet_constructs = new GarnetConstructs(this, 'CommonContructs')

    let garnet_broker_stack: GarnetOrion

    garnet_broker_stack = new GarnetOrion(this, 'ContextBrokerProxy', {
      vpc: garnet_constructs.vpc, 
      secret: garnet_constructs.secret
    })
    
    const garnet_iot_stack  = new GarnetIotStack(this, 'GarnetIoT', {
      dns_context_broker: Parameters.amazon_eks_cluster_load_balancer_dns,//garnet_broker_stack.dns_context_broker, 
      vpc: garnet_constructs.vpc, 
      api_ref: garnet_broker_stack.api_ref,
      bucket_name: garnet_constructs.bucket_name
    })

    new CfnOutput(this, 'GarnetEndpoint', {
      value: garnet_broker_stack.broker_api_endpoint,
      description: 'Garnet Unified API to access the Context Broker and Garnet IoT Capabilities'
    })

    new CfnOutput(this, 'GarnetIotQueueArn', {
      value: garnet_iot_stack.iot_sqs_endpoint_arn,
      description: 'Garnet IoT SQS Queue ARN to connect your Data Producers'
    })

    new CfnOutput(this, 'GarnetPrivateSubEndpoint', {
      value: garnet_constructs.private_sub_endpoint,
      description: 'Garnet Private Notification Endpoint for Secured Subscriptions. Only accessible within the Garnet VPC'
    })

    new CfnOutput(this, 'GarnetVersion', {
      value: `${Parameters.garnet_version}`,
      description: 'Garnet Version'
    })
    

  }
}
