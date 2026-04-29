package com.example.aws.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.*;
import software.amazon.awssdk.services.lambda.model.Runtime;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class LambdaServiceImpl implements LambdaService{
    private final LambdaClient lambdaClient;

    @Override
    public Map<String, String> createFunction(String name, String roleArn, Runtime runtime, String handler,byte[] zipBytes) {
        CreateFunctionRequest request = CreateFunctionRequest.builder()
                .functionName(name)
                .runtime(runtime)
                .handler(handler) //call to launch the action, ex in java "com.example.Handler::handleRequest"
                .role(roleArn)  // IAM Arn role is mandatory
                .code(FunctionCode.builder()
                        .zipFile(SdkBytes.fromByteArray(zipBytes))  // function code
                        .build())
                .memorySize(128)  // Minimum to save cost
                .timeout(10)  // short timeout for test
                .build();
        CreateFunctionResponse response = lambdaClient.createFunction(request);
        return Map.of("functionName",response.functionName(),
                "arn",response.functionArn(),
                "runtime",response.runtimeAsString(),
                "handler",response.handler(),
                "role",response.role(),
                "lastModified", response.lastModified());
    }

    @Override
    public List<Map<String, String>> listFunctions() {
        return lambdaClient.listFunctions().functions().stream().map(fc -> Map.of(
                "functionName",fc.functionName(),
                "arn",fc.functionArn(),
                "runtime",fc.runtimeAsString(),
                "handler",fc.handler(),
                "role",fc.role(),
                "lastModified",fc.lastModified().toString()
        )).toList();
    }

    @Override
    public Map<String, String> getFunctionDetails(String name) {
        GetFunctionRequest request = GetFunctionRequest.builder().functionName(name).build();
        GetFunctionResponse response = lambdaClient.getFunction(request);
        return Map.of("functionName",response.configuration().functionName(),
                "arn",response.configuration().functionArn(),
                "runtime",response.configuration().runtimeAsString(),
                "handler",response.configuration().handler(),
                "role",response.configuration().role(),
                "lastModified",response.configuration().lastModified().toString());
    }

    @Override
    public void deleteFunction(String name) {
        DeleteFunctionRequest request = DeleteFunctionRequest.builder().functionName(name).build();
        lambdaClient.deleteFunction(request);

    }

    @Override
    public String invokeFunction(String name, String payload) {
        InvokeRequest.Builder builder = InvokeRequest.builder().functionName(name);
        if(payload!=null && !payload.isBlank()){
            builder.payload(SdkBytes.fromString(payload, Charset.defaultCharset()));
        }

        InvokeResponse response = lambdaClient.invoke(builder.build());
        return response.payload().toString();
    }
}
