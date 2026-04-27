package com.example.aws.service;

import com.example.aws.model.PagedResponse;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface S3Service {

    List<String> listBuckets();

    String createBucket(String bucketName);

    Boolean deleteBucket(String bucketName);

    void uploadFile(String bucketName, String key, Path filePath);

    byte[] downloadFile(String bucketName, String key) throws IOException;

    String generatePresignedUrl(String bucketName, String key);

    PagedResponse listObjects(String bucketName, String continuationToken, int maxKeys);

    void AddTagToObject(String bucketName, String key, Map<String, String> newtags);

    void removeTagToObject(String bucketName, String key, String tag);

    Map<String, String> getObjectTags(String bucketName, String key);

    String getBucketPolicy(String bucketName);
}
