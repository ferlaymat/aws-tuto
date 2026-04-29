package com.example.aws.controller;

import com.example.aws.service.DynamoDBService;
import com.example.aws.service.Ec2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.ec2.model.KeyFormat;
import software.amazon.awssdk.services.ec2.model.Protocol;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1/dynamodb")
public class DynamoDBController {

    private final DynamoDBService dynamoDBService;

    @GetMapping("/table")
    public List<String> listTables(){
        return dynamoDBService.listTables();
    }
}
