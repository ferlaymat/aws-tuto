package com.example.aws.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class Ec2ServiceImpl implements Ec2Service{

    private final Ec2Client ec2Client;
    @Override
    public List<String> listInstances() {
        log.info("Listing EC2 instances...");

        DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                .build();

        return ec2Client.describeInstances(request).reservations().stream()
                .flatMap(reservation -> reservation.instances().stream())
                .map(instance -> instance.instanceId())
                .toList();
    }

    @Override
    public List<Map<String, String>> listInstancesDetails() {
        return List.of();
    }

    @Override
    public String createInstance(String instanceName, String amiId, String instanceType, String keyName) {
        log.info("Creating EC2 instance: Name={}, AMI={}, Type={}, Key={}",instanceName, amiId, instanceType, keyName);
        Tag nameTag = Tag.builder()
                .key("Name")
                .value(instanceName)
                .build();


        RunInstancesRequest.Builder builder = RunInstancesRequest.builder()
                .imageId(amiId)
                .instanceType(InstanceType.fromValue(instanceType))
                .tagSpecifications(TagSpecification.builder()
                        .resourceType(ResourceType.INSTANCE)
                        .tags(nameTag)
                        .build())
                .minCount(1)
                .maxCount(1);

        //be careful! No ssh available without key pair
        if (keyName != null && !keyName.isBlank()) {
            builder.keyName(keyName);
        }

        RunInstancesResponse response = ec2Client.runInstances(builder.build());
        String instanceId = response.instances().get(0).instanceId();
        log.info("Instance created: {}", instanceId);
        return instanceId;

    }

    @Override
    public void startInstance(String instanceId) {

    }

    @Override
    public void stopInstance(String instanceId) {

    }

    @Override
    public void rebootInstance(String instanceId) {

    }

    @Override
    public void terminateInstance(String instanceId) {
        log.info("Terminating instance: {}", instanceId);

        TerminateInstancesRequest request = TerminateInstancesRequest.builder()
                .instanceIds(instanceId)
                .build();

        ec2Client.terminateInstances(request);
        log.info("Instance {} terminating...", instanceId);

    }

    @Override
    public String getInstanceStatus(String instanceId) {
        return "";
    }

    @Override
    public String createKeyPair(String keyName) {
        return "";
    }

    @Override
    public List<String> listKeyPairs() {
        return List.of();
    }
}
