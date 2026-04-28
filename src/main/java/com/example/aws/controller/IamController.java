package com.example.aws.controller;

import com.example.aws.service.IamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1/iam")
public class IamController {

    private final IamService iamService;

    @GetMapping("/user")
    public Map<String, String> listUsers() {
        return iamService.listUsers();
    }

    @PostMapping("/user/{userName}")
    public Map<String, String> createUser(@PathVariable String userName){
        return iamService.createUser(userName);
    }

    //userName is unique for an aws account
    @GetMapping("/user/{userName}/details")
    public Map<String, String> getUserDetails(@PathVariable String userName){
        return iamService.getUserDetails(userName);
    }

    @DeleteMapping("/user/{userName}")
    public void deleteUser(@PathVariable String userName){
        iamService.deleteUser(userName);
    }
}
