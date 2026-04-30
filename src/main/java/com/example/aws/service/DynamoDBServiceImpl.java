package com.example.aws.service;

import com.example.aws.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
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
        DeleteTableRequest request = DeleteTableRequest.builder().tableName(tableName).build();
        dynamoDbClient.deleteTable(request);
    }

    @Override
    public List<String> listTables() {
        ListTablesResponse response = dynamoDbClient.listTables();
        return response.tableNames();
    }

    @Override
    public void saveProduct(Product product) {
        TableSchema<Product> productTableSchema = TableSchema.fromBean(Product.class);
        DynamoDbTable<Product> productTable = dynamoDbEnhancedClient.table("Products", productTableSchema);

        TransactWriteItemsEnhancedRequest request = TransactWriteItemsEnhancedRequest.builder().addPutItem(productTable, product).build();
        dynamoDbEnhancedClient.transactWriteItems(request);
        //can be done with a simple productTable.getItem() but not manage atomicity
    }

    @Override
    public Optional<Product> getProduct(String id, String category) {
        TableSchema<Product> productTableSchema = TableSchema.fromBean(Product.class);
        DynamoDbTable<Product> productTable = dynamoDbEnhancedClient.table("Products", productTableSchema);

        Product product = productTable.getItem(Key.builder().partitionValue(id).sortValue(category).build());

        return Optional.ofNullable(product);

    }

    @Override
    public void deleteProduct(String id, String category) {
        TableSchema<Product> productTableSchema = TableSchema.fromBean(Product.class);
        DynamoDbTable<Product> productTable = dynamoDbEnhancedClient.table("Products", productTableSchema);

        TransactWriteItemsEnhancedRequest request = TransactWriteItemsEnhancedRequest.builder().addDeleteItem(productTable, Key.builder().partitionValue(id).sortValue(category).build()).build();
        dynamoDbEnhancedClient.transactWriteItems(request);
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
