package com.example.aws.service;

import java.util.List;
import java.util.Map;

public interface Ec2Service {

    // List all instances
    List<String> listInstances();

    // List all instances with details
    List<Map<String, String>> listInstancesDetails();

    // Create an instance
    String createInstance(String instanceName, String amiId, String instanceType, String keyName);

    // Start an instance
    void startInstance(String instanceId);

    // Stop an instance
    void stopInstance(String instanceId);

    // Restart an instance
    void rebootInstance(String instanceId);

    // Delete an instance
    void terminateInstance(String instanceId);

    // Get status of an instance
    String getInstanceStatus(String instanceId);

    // Create key pair
    String createKeyPair(String keyName);

    // List all key pair
    List<String> listKeyPairs();
}
