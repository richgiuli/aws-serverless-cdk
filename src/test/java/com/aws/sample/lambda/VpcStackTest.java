package com.aws.sample.lambda;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import software.amazon.awscdk.core.App;

import java.io.IOException;

public class VpcStackTest {
    private final static ObjectMapper JSON =
        new ObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true);

    @Test
    public void testStack() throws IOException {
        App app = new App();
        SystemParameters parameters = SystemParameters.getParameters("test");
        VpcStack vpcStack = new VpcStack(app, "testVpc", parameters);

        JSON.valueToTree(app.synth().getStackArtifact(vpcStack.getArtifactId()).getTemplate());
    }
}
