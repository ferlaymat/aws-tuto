package com.example.aws.controller;

import com.example.aws.service.Ec2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.ec2.model.KeyFormat;
import software.amazon.awssdk.services.ec2.model.Protocol;
import software.amazon.awssdk.services.ec2.model.SecurityGroupRule;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1/ec2")
public class Ec2Controller {

    private final Ec2Service ec2Service;

    @GetMapping("/instance")
    public List<String> listInstances() {
        return ec2Service.listInstances();
    }

    @GetMapping("/instance/details")
    List<Map<String, String>> listInstancesDetails() {
        return ec2Service.listInstancesDetails();
    }

    @PostMapping("/instance/{instanceName}")
    public String createInstance(@PathVariable String instanceName,
                                 @RequestParam(defaultValue = "ami-098e39bafa7e7303d") String amiId,
                                 @RequestParam(defaultValue = "t2.micro") String instanceType,
                                 @RequestParam(required = false) String keyName,
                                 @RequestParam(required = false) String securityGroupId,
                                 @RequestParam(defaultValue = "false") Boolean withNginxScript) {
        return ec2Service.createInstance(instanceName, amiId, instanceType, keyName, securityGroupId, withNginxScript);
    }


    @DeleteMapping("/instance/{id}")
    public void terminateInstance(@PathVariable String id) {
        ec2Service.terminateInstance(id);
    }


    @PostMapping("/instance/{instanceId}/start")
    public void startInstance(@PathVariable String instanceId) {
        ec2Service.startInstance(instanceId);
    }

    @PostMapping("/instance/{instanceId}/stop")
    public void stopInstance(@PathVariable String instanceId) {
        ec2Service.stopInstance(instanceId);
    }

    @PostMapping("/instance/{instanceId}/reboot")
    public void rebootInstance(@PathVariable String instanceId) {
        ec2Service.rebootInstance(instanceId);
    }


    @GetMapping("/instance/{instanceId}/status")
    public String getInstanceStatus(@PathVariable String instanceId) {
        return ec2Service.getInstanceStatus(instanceId);
    }

    //keyFormat must equals PPK or PEM
    @PostMapping("/keypair/{keyName}/format/{keyFormat}")
    public String createKeyPair(@PathVariable String keyName, @PathVariable KeyFormat keyFormat) {
        return ec2Service.createKeyPair(keyName, keyFormat);
    }

    @GetMapping("/keypair")
    public Map<String, String> listKeyPairs() {
        return ec2Service.listKeyPairs();
    }

    @DeleteMapping("/keypair/{keyPairId}")
    public void deleteKeyPair(@PathVariable String keyPairId) {
        ec2Service.deleteKeyPair(keyPairId);
    }

    @PostMapping("/secgroup/{groupName}")
    public String createSecurityGroup(@PathVariable String groupName, @RequestBody String description, @RequestParam(required = false) String vpcId){
        return ec2Service.createSecurityGroup(groupName,description,vpcId);
    }

    @DeleteMapping("/secgroup/{groupId}")
    void deleteSecurityGroup(@PathVariable String groupId){
        ec2Service.deleteSecurityGroup(groupId);
    }

    @GetMapping("/secgroup")
    public List<Map<String, String>> listSecurityGroups(){
        return ec2Service.listSecurityGroups();
    }

    @PutMapping("/secgroup/{groupId}/ingress")
    public void authorizeIngress(@PathVariable String groupId, @RequestParam(defaultValue = "0.0.0.0/0") String cidrIp, @RequestParam(defaultValue = "80") int port, @RequestParam(defaultValue = "TCP") Protocol protocol){
        ec2Service.authorizeIngress(groupId,cidrIp,port,protocol);
    }

    @GetMapping("/secgroup/{groupId}")
    public List<String> findRuleId(@PathVariable String groupId, @RequestParam(defaultValue = "false") Boolean isEgress){
        return ec2Service.findRuleId(groupId, isEgress);
    }

    @PutMapping("/secgroup/{groupId}/ingress/{ruleId}")
    public void revokeIngress(@PathVariable String groupId, @PathVariable String ruleId){
        ec2Service.revokeIngress(groupId, ruleId);
    }
}
