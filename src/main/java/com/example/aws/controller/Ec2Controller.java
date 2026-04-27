package com.example.aws.controller;

import com.example.aws.service.Ec2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

}
