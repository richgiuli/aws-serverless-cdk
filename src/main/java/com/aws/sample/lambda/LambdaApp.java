package com.aws.sample.lambda;

import software.amazon.awscdk.core.App;

import java.io.IOException;

public final class LambdaApp {
    public static void main(final String[] args) throws IOException {
        App app = new App();

        SystemParameters systemParameters = SystemParameters.getParameters("dev");
        VpcStack vpcStack = new VpcStack(app, "LambdaVpcStack", systemParameters);
        new LambdaStack(app, "LambdaStack", systemParameters, vpcStack);

        app.synth();
    }
}
