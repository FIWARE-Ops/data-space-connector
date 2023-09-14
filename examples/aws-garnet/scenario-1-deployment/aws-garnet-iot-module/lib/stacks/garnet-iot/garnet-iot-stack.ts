import { Aws, NestedStack, NestedStackProps } from 'aws-cdk-lib'
import { Vpc } from 'aws-cdk-lib/aws-ec2'
import { Bucket } from 'aws-cdk-lib/aws-s3'
import { Construct } from 'constructs'
import { Parameters } from '../../../parameters'
import { GarnetIotApi } from './garnet-iot-api'
import { GarnetIot } from './garnet-iot-core'

export interface GarnetIotStackProps extends NestedStackProps {
  dns_context_broker: string,
  vpc: Vpc, 
  api_ref: string 
  bucket_name: string
}

export class GarnetIotStack extends NestedStack {

  public readonly iot_sqs_endpoint_arn : string

  constructor(scope: Construct, id: string, props: GarnetIotStackProps) {
    super(scope, id, props)

    /*
    *  CREATE GARNET IoT DATALAKE BUCKET 
    */

    // DEFAULT BUCKET NAME
    const bucket_name = props.bucket_name
    const bucket = Bucket.fromBucketName(this, 'IoTBucket', bucket_name)


    // DEPLOY THE CORE OF GARNET IoT
    const garnet_iot_core_construct = new GarnetIot(this, 'Core', {
      vpc: props.vpc, 
      dns_context_broker: props.dns_context_broker,
      bucket_arn: bucket.bucketArn
    })

    // DEPLOY THE GARNET IoT API DEVICE MANAGEMENT
    const garnet_iot_api_construct= new GarnetIotApi(this, 'Api', {
      api_ref: props.api_ref,
      vpc: props.vpc, 
      dns_context_broker: props.dns_context_broker
    })


    this.iot_sqs_endpoint_arn = garnet_iot_core_construct.sqs_garnet_iot_arn
  }
}
