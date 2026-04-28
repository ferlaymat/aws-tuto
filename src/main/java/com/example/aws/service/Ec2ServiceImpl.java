package com.example.aws.service;

import com.example.aws.util.CidrValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
    public String createInstance(String instanceName, String amiId, String instanceType, String keyName, String securityGroupId, Boolean withNginxScript) {
        log.info("Creating EC2 instance: Name={}, AMI={}, Type={}, Key={}, securityGroupId={}, withNginxScript={}", instanceName, amiId, instanceType, keyName, securityGroupId, withNginxScript);
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

        if (securityGroupId != null && !securityGroupId.isBlank()) {
            builder.securityGroupIds(securityGroupId);
        }


        if (withNginxScript) {
            String script = """
                    #!/bin/bash
                    yum update -y
                    yum install -y nginx
                    service nginx start
                    """;

            //encoding script in base64
            String encodedUserData = Base64.getEncoder()
                    .encodeToString(script.getBytes(StandardCharsets.UTF_8));

            builder.userData(encodedUserData);
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
        if (!response.instanceStatuses().isEmpty()) {
            return response.instanceStatuses().getFirst().instanceState().nameAsString();
        }
        return "UNKNOWN";
    }

    @Override
    public String createKeyPair(String keyName, KeyFormat keyFormat) {
        log.info("Creating KeyPair: {}", keyName);
        if (keyFormat.equals(KeyFormat.UNKNOWN_TO_SDK_VERSION)) {
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
                .stream().collect(Collectors.toMap(KeyPairInfo::keyName, KeyPairInfo::keyPairId));
    }

    @Override
    public void deleteKeyPair(String keyPairId) {
        log.info("Deleting KeyPair: {}", keyPairId);
        DeleteKeyPairRequest request = DeleteKeyPairRequest.builder().keyPairId(keyPairId).build();
        ec2Client.deleteKeyPair(request);
        log.info("KeyPair {} deleted...", keyPairId);
    }


    @Override
    //create a security group
    public String createSecurityGroup(String groupName, String description, String vpcId) {
        log.info("Creating security group: {} ,desc: {} ,vpc: {}", groupName, description, vpcId);
        CreateSecurityGroupRequest.Builder builder = CreateSecurityGroupRequest.builder()
                .groupName(groupName)
                .description(description);

        if (vpcId != null && !vpcId.isBlank()) { //by default, default VPC
            builder
                    .vpcId(vpcId);
        }

        CreateSecurityGroupRequest request = builder.build();

        CreateSecurityGroupResponse response = ec2Client.createSecurityGroup(request);
        log.info("Security group {} created...", groupName);
        return response.groupId();
    }

    @Override
    public List<Map<String, String>> listSecurityGroups() {
        DescribeSecurityGroupsRequest request = DescribeSecurityGroupsRequest.builder()
                .build();

        return ec2Client.describeSecurityGroups(request).securityGroups().stream()
                .map(sg -> {
                    Map<String, String> details = new HashMap<>();
                    details.put("groupId", sg.groupId());
                    details.put("groupName", sg.groupName());
                    details.put("description", sg.description());
                    details.put("vpcId", sg.vpcId());
                    return details;
                })
                .toList();
    }


    @Override
    //delete a security group
    public void deleteSecurityGroup(String groupId) {
        log.info("Deleting security group: {}", groupId);
        DeleteSecurityGroupRequest request = DeleteSecurityGroupRequest.builder()
                .groupId(groupId).build();

        ec2Client.deleteSecurityGroup(request);
        log.info("Security group: {} deleted...", groupId);
    }

    @Override
    //create ingress rules, ex  HTTP (port 80): authorizeIngress(groupId, "0.0.0.0/0",80);
    public void authorizeIngress(String groupId, String cidrIp, int port, Protocol protocol) {
        log.info("Creating ingress {}/{} for security group : {}", cidrIp, port, groupId);
        if (protocol.equals(Protocol.UNKNOWN_TO_SDK_VERSION)) {
            throw new IllegalArgumentException("unknown protocol format");
        }
        if (!CidrValidator.isValidCidr(cidrIp)) {
            throw new IllegalArgumentException("Invalid format for cidrIp: " + cidrIp);
        }
        AuthorizeSecurityGroupIngressRequest request = AuthorizeSecurityGroupIngressRequest.builder()
                .groupId(groupId)
                .ipPermissions(IpPermission.builder()
                        .ipProtocol(protocol.name())
                        .fromPort(port)
                        .toPort(port)
                        .ipRanges(IpRange.builder()
                                .cidrIp(cidrIp)  // ex: "0.0.0.0/0" = everywhere
                                .build())
                        .build())
                .build();

        ec2Client.authorizeSecurityGroupIngress(request);
        log.info("Ingress {}/{} for security group : {} created...", cidrIp, port, groupId);
    }

    @Override
    public List<String> findRuleId(String groupId, Boolean isEgress) {
        DescribeSecurityGroupRulesRequest request = DescribeSecurityGroupRulesRequest.builder()
                .filters(
                        Filter.builder()
                                .name("group-id")
                                .values(groupId)
                                .build()
                )
                .build();
        DescribeSecurityGroupRulesResponse response = ec2Client.describeSecurityGroupRules(request);
        if (isEgress) {
            return response.securityGroupRules().stream().filter(SecurityGroupRule::isEgress).map(SecurityGroupRule::securityGroupRuleId).toList();
        }
        return response.securityGroupRules().stream().filter(sgr -> !sgr.isEgress()).map(SecurityGroupRule::securityGroupRuleId).toList();
    }


    @Override
    public void revokeIngress(String groupId, String ruleId) {
        log.info("Revoking ingress {} for security group : {}", ruleId, groupId);
        RevokeSecurityGroupIngressRequest request = RevokeSecurityGroupIngressRequest.builder()
                .groupId(groupId)
                .securityGroupRuleIds(ruleId)  // Rule ID unique
                .build();


        ec2Client.revokeSecurityGroupIngress(request);
        log.info("Ingress {} for security group : {} revoked...", ruleId, groupId);
    }

}
