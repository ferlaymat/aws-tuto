package com.example.aws.model;

import com.example.aws.config.AutoCreateTable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.math.BigDecimal;

//Do not use lombok with dynamoDB cause annotation scan get/is prefixed methods and is not able to scan generated methods
@DynamoDbBean  //equivalent to entity or document
@AutoCreateTable(tableName = "Products") //custom annotation to allow auto generation of table in db
public class Product {

    private String id;
    private String name;
    private String category;
    private BigDecimal price;
    private String description;

    // Partition key is mandatory
    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Sort key is optional and unique (done for composite key) -> create secondary index
    @DynamoDbSortKey
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // Allow mapping with item field in db with a different name. Use it carefully
    @DynamoDbAttribute("product_name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
