package com.aws.sample.lambda;

import lombok.Getter;
import software.amazon.awscdk.core.*;
import software.amazon.awscdk.services.ec2.SubnetConfiguration;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;

import java.util.Arrays;

public class VpcStack extends Stack {
    @Getter
    private final Vpc vpc;

    public VpcStack(Construct parent,
                    String id,
                    SystemParameters parameters) {
        super(parent, id, null);

        parameters.addTags(this, "vpc");

        String vpcCidr = parameters.getLiteralString("vpc/cidr");
        String vpcPublicPrefix = parameters.getLiteralString("vpc/public-subnets-prefix");
        String vpcPrivatePrefix = parameters.getLiteralString("vpc/private-subnets-prefix");

        // XXX Note that creating a whole new VPC won't work with peering ATM, see:
        // https://docs.aws.amazon.com/AmazonRDS/latest/AuroraUserGuide/aurora-serverless.html#aurora-serverless.limitations
        // This assumes Cloud9 will be launched in the VPC, done below

        // XXX VPC does not support setting tags on creation, see:
        // https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/Using_Tags.html
        vpc = Vpc.Builder.create(this, "main-vpc").cidr(vpcCidr).
                subnetConfiguration(Arrays.asList(
                        SubnetConfiguration.builder().subnetType(SubnetType.PUBLIC).name(vpcPublicPrefix).build(),
                        SubnetConfiguration.builder().subnetType(SubnetType.PRIVATE).name(vpcPrivatePrefix).build())).
                build();
    }
}
