package com.aws.sample.lambda;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Tag;
import software.amazon.awscdk.services.ssm.StringParameter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

@Data
public class SystemParameters {
    private final String deployEnv;
    private String deployInstanceTagName = "cdk:deployInstance";
    private final String deployEnvTagName = "cdk:deployFor";
    private final String customerTagName = "cdk:deployFor";
    private final String customerTag = "FireEye";

    private final Map<String, String> parameters;

    public SystemParameters(String jsonParams, String deployEnv) throws IOException {
        this.deployEnv = deployEnv;
        parameters = new HashMap<>();
        new ObjectMapper().readTree(new StringReader(jsonParams)).forEach(param->{
            parameters.put(param.get("Name").textValue(), param.get("Value").textValue());
        });
    }

    void addTags(Construct scope, String instanceType) {
        String deployEnv = getDeployEnv();
        CfnOutput.Builder.create(scope, "deployEnv").value(deployEnv).build();
        Tag.add(scope, getDeployInstanceTagName(), instanceType);
        Tag.add(scope, getDeployEnvTagName(), deployEnv);
        Tag.add(scope, getCustomerTagName(), getCustomerTag());
    }

    public String getLiteralString(String paramName) {
        String paramValue = parameters.get(paramName);
        if (paramValue == null) {
            throw new IllegalArgumentException("Missing parameter " + paramName);
        }
        return paramValue;
    }

    public String getStringRef(Construct scope, String paramName) {
        return StringParameter.valueForStringParameter(scope,"/deploy/" + deployEnv + "/" + paramName);
    }

    public String getSecureStringRef(Construct scope, String paramName) {
        return StringParameter.valueForSecureStringParameter(scope,
                "/deploy/" + deployEnv + "/" + paramName,
                1);
    }

    static SystemParameters getParameters(String deployEnv) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                SystemParameters.class.getClassLoader().getResourceAsStream("parameters.json")));
        String line = reader.readLine();
        StringBuilder json = new StringBuilder();
        while (line != null) {
            json.append(line);
            json.append("\n");
            line = reader.readLine();
        }
        return new SystemParameters(json.toString(), deployEnv);
    }
}
