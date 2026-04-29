package com.example.aws.service;

import com.example.aws.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface DynamoDBService {

    // Table management
    void createTable(String tableName);

    void deleteTable(String tableName);

    List<String> listTables();

    // CRUD
    void saveProduct(Product product);

    Optional<Product> getProduct(String id, String category);

    void deleteProduct(String id, String category);

    List<Product> getProductsByCategory(String category);

    // Advanced
    List<Product> queryProductsBetweenPrice(String category, BigDecimal min, BigDecimal max);
}

