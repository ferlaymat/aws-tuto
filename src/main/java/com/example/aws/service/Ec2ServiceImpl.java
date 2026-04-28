package com.example.aws.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class Ec2ServiceImpl implements Ec2Service {

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
        DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                .build();

        return ec2Client.describeInstances(request).reservations().stream()
                .flatMap(reservation -> reservation.instances().stream())
                .map(instance -> {
                    //non-exhaustive list of properties. You can add or remove as you want
                    Map<String, String> details = new HashMap<>();
                    details.put("instanceId", instance.instanceId());
                    details.put("instanceType", instance.instanceType().toString());
                    details.put("state", instance.state().nameAsString());
                    details.put("publicIpAddress", instance.publicIpAddress() != null ? instance.publicIpAddress() : "N/A");
                    details.put("privateIpAddress", instance.privateIpAddress() != null ? instance.privateIpAddress() : "N/A");
                    details.put("imageId", instance.imageId());
                    return details;
                })

                .toList();
    }

    @Override
    public String createInstance(String instanceName, String amiId, String instanceType, String keyName) {
        log.info("Creating EC2 instance: Name={}, AMI={}, Type={}, Key={}", instanceName, amiId, instanceType, keyName);
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
        log.info("Starting instance: {}", instanceId);

        StartInstancesRequest request = StartInstancesRequest.builder()
                .instanceIds(instanceId)
                .build();

        ec2Client.startInstances(request);
        log.info("Instance {} started...", instanceId);
    }

    @Override
    public void stopInstance(String instanceId) {
        log.info("Stoping instance: {}", instanceId);

        StopInstancesRequest request = StopInstancesRequest.builder()
                .instanceIds(instanceId)
                .build();

        ec2Client.stopInstances(request);
        log.info("Instance {} stopped...", instanceId);
    }

    @Override
    public void rebootInstance(String instanceId) {
        log.info("Rebooting instance: {}", instanceId);

        RebootInstancesRequest request = RebootInstancesRequest.builder()
                .instanceIds(instanceId)
                .build();

        ec2Client.rebootInstances(request);
        log.info("Instance {} rebooted...", instanceId);
    }

    @Override
    public void terminateInstance(String instanceId) {
        log.info("Terminating instance: {}", instanceId);

        TerminateInstancesRequest request = TerminateInstancesRequest.builder()
                .instanceIds(instanceId)
                .build();

        ec2Client.terminateInstances(request);
        log.info("Instance {} terminated...", instanceId);

    }

    @Override
    public String getInstanceStatus(String instanceId) {

        DescribeInstanceStatusRequest request = DescribeInstanceStatusRequest.builder()
                .instanceIds(instanceId)
                .build();

        DescribeInstanceStatusResponse response = ec2Client.describeInstanceStatus(request);
        if(!response.instanceStatuses().isEmpty())
        {
            return response.instanceStatuses().getFirst().instanceState().nameAsString();
        }
        return "UNKNOWN";
    }

    @Override
    public String createKeyPair(String keyName, KeyFormat keyFormat) {
        log.info("Creating KeyPair: {}", keyName);
        if(keyFormat.equals(KeyFormat.UNKNOWN_TO_SDK_VERSION)){
            throw new IllegalArgumentException("unknown key format");
        }
        CreateKeyPairRequest request = CreateKeyPairRequest.builder()
                .keyName(keyName)
                .keyFormat(keyFormat)
                .build();
        CreateKeyPairResponse response = ec2Client.createKeyPair(request);
        log.info("KeyPair {} created...", keyName);
        return response.keyMaterial();
    }

    @Override
    public Map<String, String> listKeyPairs() {
        DescribeKeyPairsRequest request = DescribeKeyPairsRequest.builder()
                .build();
       return ec2Client.describeKeyPairs(request).keyPairs()
                .stream().collect(Collectors.toMap(KeyPairInfo::keyName,KeyPairInfo::keyPairId));
    }

    @Override
    public void deleteKeyPair(String keyPairId){
        log.info("Deleting KeyPair: {}", keyPairId);
        DeleteKeyPairRequest request = DeleteKeyPairRequest.builder().keyPairId(keyPairId).build();
        ec2Client.deleteKeyPair(request);
        log.info("KeyPair {} deleted...", keyPairId);
    }
}
