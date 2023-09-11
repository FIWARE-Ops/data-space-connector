import { Aws } from "aws-cdk-lib"
import { InstanceClass, InstanceSize, InstanceType } from "aws-cdk-lib/aws-ec2"
import {StorageType } from "aws-cdk-lib/aws-rds"


enum Broker {
    Orion = "Orion", // Here orion refers to orion-ld broker:  https://github.com/FIWARE/context.Orion-LD
    Scorpio = "Scorpio"
}

export const Parameters = {
    
    // FIWARE DATA SPACE CONNECTOR PARAMETERS
    amazon_eks_cluster_load_balancer_dns: "k8s-kubesyst-ingressn-XXXXXXXX.elb.eu-west-1.amazonaws.com", // REPLACE WITH YOUR CLUSTER LOAD BALANCER DNS
    amazon_eks_cluster_load_balancer_listener_arn: "arn:aws:elasticloadbalancing:eu-west-1:XXXXXXXXX:listener/net/k8s-kubesyst-ingressn-XXXXXXXXXX/XXXXXXXXXXXXXXXX/XXXXXXXXXXXXXXXX",
    
    // GARNET PARAMETERS
    aws_region: "eu-west-1", // see regions in which you can deploy Garnet: https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-vpc-links.html#http-api-vpc-link-availability
    garnet_version: "1.0.0", // Do not change
    garnet_broker: Broker.Orion, // choose between enum Broker value (Orion or Scorpio) 
    garnet_bucket: `garnet-iot-datalake-${Aws.REGION}-${Aws.ACCOUNT_ID}`, // Default name, change only if really needed.
    //GARNET IOT PARAMETERS
    garnet_iot: {
        shadow_prefix: "Garnet", // Do not change. 
        smart_data_model_url : 'https://raw.githubusercontent.com/smart-data-models/data-models/master/context.jsonld'
    },
    // FARGATE PARAMETERS
    garnet_fargate: {
        fargate_desired_count: 2,
        fargate_cpu: 1024,
        fargate_memory_limit: 4096
    },
    // SCORPIO BROKER PARAMETERS
    garnet_scorpio: {
        image_context_broker: 'public.ecr.aws/scorpiobroker/all-in-one-runner:java-sqs-testsqs', // Link to ECR Public gallery of Scorpio Broker image.
        rds_instance_type: InstanceType.of( InstanceClass.BURSTABLE4_GRAVITON, InstanceSize.SMALL), // see https://aws.amazon.com/rds/instance-types/
        rds_storage_type: StorageType.GP3, // see https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_Storage.html
        dbname: 'scorpio'
    },
    // ORION-LD PARAMETERS
    garnet_orion: {
        image_context_broker: 'fiware/orion-ld:1.5.0-PRE-1455', // Link to ECR Public gallery of Orion image.
        docdb_instance_type: InstanceType.of( InstanceClass.BURSTABLE4_GRAVITON, InstanceSize.MEDIUM), // https://docs.aws.amazon.com/documentdb/latest/developerguide/db-instance-classes.html 
        docdb_nb_instances: 2
    }

}