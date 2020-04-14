package com.myorg;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.s3.Bucket;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class LambdaStack extends Stack {
    @SuppressWarnings("ConstantConditions")
    public LambdaStack(final Construct parent, final String id, SystemParameters parameters, VpcStack vpcStack) {
        super(parent, id, null);

        parameters.addTags(this, "lambda");

        Bucket bucket = new Bucket(this, "aws-sa-west-ricgiuli-lambda");

        String functionName = "TestLambda";


        Role lambdaRole = Role.Builder.create(this, "test-lamba-role").
                assumedBy(ServicePrincipal.Builder.create("lambda.amazonaws.com").build()).inlinePolicies(
                Collections.singletonMap("test-lambda-access-policy", PolicyDocument.Builder.create().statements(
                        Arrays.asList(
                                PolicyStatement.Builder.create().
                                        effect(Effect.ALLOW).
                                        actions(Arrays.asList("s3:GetObject", "s3:PutObject", "s3:DeleteObject", "s3:PutObjectTagging")).
                                        resources(Arrays.asList(bucket.getBucketArn() + "*", bucket.getBucketArn() + "*")).build(),
                                PolicyStatement.Builder.create().
                                        effect(Effect.ALLOW).
                                        actions(Collections.singletonList("s3:ListBucket")).
                                        resources(Arrays.asList(bucket.getBucketArn() + "*", bucket.getBucketArn() + "*")).build(),
                                PolicyStatement.Builder.create().
                                        effect(Effect.ALLOW).
                                        actions(Collections.singletonList("logs:CreateLogGroup")).
                                        resources(Collections.singletonList("arn:aws:logs:us-east-1:450691856460:*")).build(),
                                PolicyStatement.Builder.create().
                                        effect(Effect.ALLOW).
                                        actions(Arrays.asList("logs:CreateLogStream", "logs:PutLogEvents")).
                                        resources(Collections.singletonList("arn:aws:logs:us-east-1:450691856460:log-group:/aws/lambda/" + functionName + ":*")).build()
                        )
                ).build())).managedPolicies(
                Collections.singletonList(ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaVPCAccessExecutionRole"))).
                permissionsBoundary(ManagedPolicy.fromManagedPolicyName(this, "lambda-boundary-managed-policy", "lambda-boundary-managed-policy")).
                roleName("test-lamba-role").build();

        lambdaRole.getAssumeRolePolicy().addStatements(PolicyStatement.Builder.create().
                effect(Effect.ALLOW).
                actions(Collections.singletonList("sts:AssumeRole")).
                principals(Collections.singletonList(
                        User.fromUserName(this, "allow-dev", "Development"))
                ).build());

        Function handler = Function.Builder.create(this, functionName)
                .functionName(functionName)
                .runtime(Runtime.PYTHON_3_8)
                .code(Code.fromAsset("src/main/resources"))
                .environment(new HashMap<String, String>() {{
                    put("BUCKET", bucket.getBucketName());
                }})
                .role(lambdaRole)
                .handler("testLambda.handler")
                .vpc(vpcStack.getVpc())
                .vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PRIVATE).build()).build();

        bucket.grantReadWrite(handler);

        //CfnWaitCondition wait = CfnWaitCondition.Builder.create(this, "wait-for-lambda").timeout("10").build();

        RestApi api = RestApi.Builder.create(this, "Test-Lambda-API")
                .restApiName("Test Lambda Service").description("Invokes " + functionName + ".")
                .build();

        LambdaIntegration testLambdaIntegration = LambdaIntegration.Builder.create(handler)
                .requestTemplates(new HashMap<String, String>() {{
                    put("application/json", "{ \"statusCode\": \"200\" }");
                }}).build();

        api.getRoot().addMethod("GET", testLambdaIntegration);

        CfnDocumentationPart.Builder.create(this, "test-lambda-doc-api").
                restApiId(api.getRestApiId()).location(
                        CfnDocumentationPart.LocationProperty.builder().type("API").build()).
                properties("{\n" +
                        "    \"description\": \"Main API documentation\"\n" +
                        "}").build();

        CfnDocumentationPart.Builder.create(this, "test-lambda-doc").
                restApiId(api.getRestApiId()).location(
                CfnDocumentationPart.LocationProperty.builder().method("GET").type("METHOD").path("/").build()).
                properties("{\n" +
                        "    \"description\": \"GET method documentation\"\n" +
                        "}").build();

        CfnDocumentationVersion apiDocVer = CfnDocumentationVersion.Builder.create(this, "test-lambda-doc-api-v1").
                documentationVersion("v1").
                restApiId(api.getRestApiId()).
                description("test doc version").build();

        Stage.Builder.create(this, "test-api-stage").
                documentationVersion(apiDocVer.getDocumentationVersion()).
                stageName("test").
                deployment(api.getLatestDeployment()).build();
    }
}
