package com.example.aws.service;

import software.amazon.awssdk.services.lambda.model.Runtime;

import java.util.List;
import java.util.Map;

public interface LambdaService {
      // CRUD - free
      Map<String, String> createFunction(String name, String roleArn, Runtime runtime, String handler, byte[] zipBytes);
      List<Map<String, String>> listFunctions();
      Map<String, String> getFunctionDetails(String name);
      void deleteFunction(String name);

      // Invocation - not free. price = compute + request call
      String invokeFunction(String name, String payload);
  }
