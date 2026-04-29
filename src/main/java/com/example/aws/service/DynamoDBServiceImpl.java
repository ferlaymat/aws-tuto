package com.example.aws.service;

import com.example.aws.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class DynamoDBServiceImpl implements DynamoDBService {

    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private final DynamoDbClient dynamoDbClient;

    @Override
    public void createTable(String tableName) {
    }

    @Override
    public void deleteTable(String tableName) {
    }

    @Override
    public List<String> listTables() {
        ListTablesResponse response = dynamoDbClient.listTables();
        return response.tableNames();
    }

    @Override
    public void saveProduct(Product product) {

    }

    @Override
    public Optional<Product> getProduct(String id, String category) {
        return Optional.empty();
    }

    @Override
    public void deleteProduct(String id, String category) {

    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        return List.of();
    }

    @Override
    public List<Product> queryProductsBetweenPrice(String category, BigDecimal min, BigDecimal max) {
        return List.of();
    }
}
