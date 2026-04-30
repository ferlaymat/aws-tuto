package com.example.aws.controller;

import com.example.aws.dto.ProductDto;
import com.example.aws.model.Product;
import com.example.aws.service.DynamoDBService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1/dynamodb")
public class DynamoDBController {

    private final DynamoDBService dynamoDBService;

    @GetMapping("/table")
    public List<String> listTables() {
        return dynamoDBService.listTables();
    }

    @DeleteMapping("/table/{tableName}")
    public void deleteTable(@PathVariable String tableName) {
        dynamoDBService.deleteTable(tableName);
    }

    @PostMapping("/product")
    public Product saveProduct(@RequestBody ProductDto productDto) {

        Product product = new Product(productDto.name(), productDto.category(), productDto.price(), productDto.description());
        dynamoDBService.saveProduct(product);
        return product;
    }

    @GetMapping("/product/{id}/ctg/{category}")
    public Optional<Product> getProduct(@PathVariable String id, @PathVariable String category) {
        return dynamoDBService.getProduct(id, category);
    }

    @DeleteMapping("/product/{id}/ctg/{category}")
    public void deleteProduct(@PathVariable String id, @PathVariable String category) {
        dynamoDBService.deleteProduct(id, category);
    }
}
