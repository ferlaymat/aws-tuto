package com.example.aws.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DynamoDbAutoCreateTableConfig {

    private final DynamoDbClient dynamoDbClient;

    @PostConstruct
    public void createTablesIfNeeded() {
        log.info("Scanning for @AutoCreateTable entities...");
//TODO improve the way the select the package. Currently it s hardcoded and looks only in model
        Reflections reflections = new Reflections("com.example.aws.model");
        //fetch all classes annotated by AutoCreateTable
        Set<Class<?>> entities = reflections.getTypesAnnotatedWith(AutoCreateTable.class);

        for (Class<?> entityClass : entities) {
            AutoCreateTable annotation = entityClass.getAnnotation(AutoCreateTable.class);
            //get the tableName from annotation if not empty or directly from the className
            String tableName = annotation.tableName().isEmpty()
                    ? entityClass.getSimpleName()
                    : annotation.tableName();

            createTableIfNotExists(tableName, entityClass);
        }
    }

    private void createTableIfNotExists(String tableName, Class<?> entityClass) {
        ListTablesResponse response = dynamoDbClient.listTables();

        if (response.tableNames().contains(tableName)) {
            log.info("Table {} already exists", tableName);
        } else {
            log.info("Creating table {} for entity {}", tableName, entityClass.getSimpleName());
            createTable(tableName, entityClass);
        }

    }

    private void createTable(String tableName, Class<?> entityClass) {
        List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
        List<KeySchemaElement> keySchema = new ArrayList<>();

        // Scan all getters to find @DynamoDbPartitionKey and @DynamoDbSortKey
        for (Method method : entityClass.getMethods()) {
            if (method.isAnnotationPresent(DynamoDbPartitionKey.class)) {
                String attrName = extractAttributeName(method);
                attributeDefinitions.add(AttributeDefinition.builder()
                        .attributeName(attrName)
                        .attributeType(ScalarAttributeType.S) //TODO by default but try to get from getter method in a second step
                        .build());
                keySchema.add(KeySchemaElement.builder()
                        .attributeName(attrName)
                        .keyType(KeyType.HASH)
                        .build());
            }

            if (method.isAnnotationPresent(DynamoDbSortKey.class)) {
                String attrName = extractAttributeName(method);
                attributeDefinitions.add(AttributeDefinition.builder()
                        .attributeName(attrName)
                        .attributeType(ScalarAttributeType.S)
                        .build());
                keySchema.add(KeySchemaElement.builder()
                        .attributeName(attrName)
                        .keyType(KeyType.RANGE)
                        .build());
            }
        }

        if (keySchema.isEmpty()) {
            throw new IllegalStateException("No @DynamoDbPartitionKey found in " + entityClass.getName());
        }

        CreateTableRequest request = CreateTableRequest.builder()
                .tableName(tableName)
                .attributeDefinitions(attributeDefinitions)
                .keySchema(keySchema)
                .billingMode(BillingMode.PAY_PER_REQUEST) //TODO by default but try to get from annotation in a second step
                .build();

        dynamoDbClient.createTable(request);
        log.info("Table {} created successfully", tableName);
    }

    //get Attribut name from getters
    private String extractAttributeName(Method getter) {
        DynamoDbAttribute attrAnnotation = getter.getAnnotation(DynamoDbAttribute.class);
        if (attrAnnotation != null) {
            return attrAnnotation.value();
        }

        // Convert "getName" to "name", "getUserId" to "userId"
        String methodName = getter.getName();
        if (methodName.startsWith("get")) {
            return camelCase(methodName.substring(3));
        } else if (methodName.startsWith("is")) {
            return camelCase(methodName.substring(2));
        }

        throw new IllegalArgumentException("Invalid getter: " + methodName);
    }

    private String camelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return Character.toLowerCase(input.charAt(0)) + input.substring(1);
    }
}
