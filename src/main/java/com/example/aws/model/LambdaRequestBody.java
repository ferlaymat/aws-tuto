package com.example.aws.model;

import software.amazon.awssdk.services.lambda.model.Runtime;

public record LambdaRequestBody(String roleArn,
                                Runtime runtime,
                                String handler) {
}
