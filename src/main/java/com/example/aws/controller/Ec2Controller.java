package com.example.aws.controller;

import com.example.aws.service.Ec2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.ec2.model.KeyFormat;

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
    List<Map<String, String>> listInstancesDetails(){
        return ec2Service.listInstancesDetails();
    }

    @PostMapping("/instance/{instanceName}")
    public String createInstance(@PathVariable String instanceName,
                                 @RequestParam(defaultValue = "ami-098e39bafa7e7303d") String amiId,
                                 @RequestParam(defaultValue = "t2.micro") String instanceType,
                                 @RequestParam(required = false) String keyName) {
        return ec2Service.createInstance(instanceName, amiId, instanceType, keyName);
    }


    @DeleteMapping("/instance/{id}")
    public void terminateInstance(@PathVariable String id) {
        ec2Service.terminateInstance(id);
    }


    @PostMapping("/instance/{instanceId}/start")
    void startInstance(@PathVariable String instanceId){
        ec2Service.startInstance(instanceId);
    }

    @PostMapping("/instance/{instanceId}/stop")
    void stopInstance(@PathVariable String instanceId){
        ec2Service.stopInstance(instanceId);
    }

    @PostMapping("/instance/{instanceId}/reboot")
    void rebootInstance(@PathVariable String instanceId){
        ec2Service.rebootInstance(instanceId);
    }


    @GetMapping("/instance/{instanceId}/status")
    String getInstanceStatus(@PathVariable String instanceId){
        return ec2Service.getInstanceStatus(instanceId);
    }

    //keyFormat must equals PPK or PEM
    @PostMapping("/keypair/{keyName}/format/{keyFormat}")
    String createKeyPair(@PathVariable String keyName, @PathVariable KeyFormat keyFormat){
        return ec2Service.createKeyPair(keyName,keyFormat);
    }

    @GetMapping("/keypair")
    Map<String, String> listKeyPairs(){
        return ec2Service.listKeyPairs();
    }

    @DeleteMapping("/keypair/{keyPairId}")
    void deleteKeyPair(@PathVariable String keyPairId){
        ec2Service.deleteKeyPair(keyPairId);
    }
}
