package com.example.aws.controller;

import com.example.aws.service.LambdaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1/lambda")
public class LambdaController {

    private final LambdaService lambdaService;

    @GetMapping
    public List<Map<String, String>> listFunctions() {
        return lambdaService.listFunctions();
    }

    @GetMapping("/{name}")
    public Map<String, String> getFunctionDetails(@PathVariable String name) {
        return lambdaService.getFunctionDetails(name);
    }

    @DeleteMapping("/{name}")
    public void deleteFunction(@PathVariable String name) {
        lambdaService.deleteFunction(name);
    }

    @PostMapping("/{name}")
    public String invokeFunction(@PathVariable String name, @RequestBody String payload) {
        return lambdaService.invokeFunction(name, payload);
    }
}
