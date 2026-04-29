package com.example.aws.service;

import com.example.aws.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ServiceImpl implements S3Service{


    private final S3Client s3Client;
    private final S3Presigner s3Presigner;


    public List<String> listBuckets() {
        log.info("Listing S3 buckets...");
        ListBucketsResponse response = s3Client.listBuckets();
        log.info("Found {} buckets", response.buckets().size());
        return response.buckets().stream().map(Bucket::name).toList();

    }

    public String createBucket(String bucketName) {
        log.info("Creating bucket: {}", bucketName);

        CreateBucketRequest request = CreateBucketRequest.builder().bucket(bucketName).build();

        CreateBucketResponse response = s3Client.createBucket(request);
        log.info("Bucket created: {}", response.location());
        return response.bucketArn();
    }


    public Boolean deleteBucket(String bucketName) {
        log.info("Deleting bucket: {}", bucketName);

        // S3 not allow to delete a non-empty bucket, so first, we list the bucket object
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder().bucket(bucketName).build();

        //case without paginator. limit to 1000 objects so not really useful in S3 env
       /*ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
        listResponse.contents().stream()
                .forEach(obj -> {
                    log.debug("Deleting object: {}", obj.key());
                    s3Client.deleteObject(DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(obj.key())
                            .build());
                });*/

        //Case with paginator. All queries are done automatically by it but do 1 query per element
        /*s3Client.listObjectsV2Paginator(listRequest).stream()
                .flatMap(response -> response.contents().stream())
                .forEach(obj -> {
                    log.debug("Deleting object: {}", obj.key());
                    s3Client.deleteObject(DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(obj.key())
                            .build());
                });*/


        //delete by batch off 1000 elements
        s3Client.listObjectsV2Paginator(listRequest).stream()
                //iterate on each page and create a list of elements (max 1000)
                .forEach(page -> {
                    List<ObjectIdentifier> objects = page.contents().stream()
                            .map(obj -> ObjectIdentifier.builder().key(obj.key()).build())
                            .toList();
                    //if list is not empty, delete elements
                    if (!objects.isEmpty()) {
                        log.info("Deleting batch of {} objects", objects.size());

                        DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                                .bucket(bucketName)
                                .delete(Delete.builder().objects(objects).build())
                                .build();

                        s3Client.deleteObjects(deleteRequest);
                    }
                });

        //then delete the bucket
        DeleteBucketRequest request = DeleteBucketRequest.builder().bucket(bucketName).build();

        s3Client.deleteBucket(request);
        log.info("Bucket deleted");
        return true;
    }

    public void uploadFile(String bucketName, String key, Path filePath) {
        log.info("Uploading {} to {}/{}", filePath, bucketName, key);

        PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key(key).build();

        s3Client.putObject(request, RequestBody.fromFile(filePath));
        log.info("File uploaded successfully");
    }

    public byte[] downloadFile(String bucketName, String key) throws IOException {
        log.info("Downloading {}/{}", bucketName, key);

        GetObjectRequest request = GetObjectRequest.builder().bucket(bucketName).key(key).build();

        return s3Client.getObject(request).readAllBytes();
    }

    //provide a temporary url to give access to a resource inside the s3 to other users
    //the url is available only 30s
    public String generatePresignedUrl(String bucketName, String key) {


        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder().signatureDuration(Duration.ofSeconds(30)).getObjectRequest(r -> r.bucket(bucketName).key(key)).build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();

    }




    public PagedResponse listObjects(String bucketName, String continuationToken, int maxKeys) {
        ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .maxKeys(maxKeys);

        //give the offset to start at the correct index
        if (continuationToken != null) {
            requestBuilder.continuationToken(continuationToken);
        }
        //use the function without paginator to fetch page by page
        ListObjectsV2Response response = s3Client.listObjectsV2(requestBuilder.build());

        return new PagedResponse(
                response.contents().stream().map(S3Object::key).toList(),
                response.nextContinuationToken(),
                response.isTruncated()
        );
    }


    public void AddTagToObject(String bucketName, String key, Map<String, String> newtags) {
        log.info("Tagging object {}/{} with tags: {}", bucketName, key, newtags);
        // fetch existing tags in order to append
        Map<String, String> existingTags = getObjectTags(bucketName, key);

        // merge newtags and existing tags
        Map<String, String> mergedTags = new HashMap<>(newtags);
        mergedTags.putAll(existingTags);
        List<Tag> tagList = mergedTags.entrySet().stream()
                .map(entry -> Tag.builder().key(entry.getKey()).value(entry.getValue()).build())
                .toList();

        PutObjectTaggingRequest request = PutObjectTaggingRequest.builder()
                .bucket(bucketName)
                .key(key)
                //set tags - replace if already exist
                .tagging(Tagging.builder().tagSet(tagList).build())
                .build();

        s3Client.putObjectTagging(request);
        log.info("Tags added successfully");
    }



    public void removeTagToObject(String bucketName, String key, String tag) {
        log.info("Remove tag {} to object {}/{}", tag, bucketName, key);
        // fetch existing tags in order to append
        Map<String, String> existingTags = getObjectTags(bucketName, key);

        existingTags.remove(tag);

        List<Tag> tagList = existingTags.entrySet().stream()
                .map(entry -> Tag.builder().key(entry.getKey()).value(entry.getValue()).build())
                .toList();

        PutObjectTaggingRequest request = PutObjectTaggingRequest.builder()
                .bucket(bucketName)
                .key(key)
                //set tags - replace if already exist
                .tagging(Tagging.builder().tagSet(tagList).build())
                .build();

        s3Client.putObjectTagging(request);
        log.info("Tags removed successfully");
    }

    // List all tags of an object
    public Map<String, String> getObjectTags(String bucketName, String key) {
        GetObjectTaggingRequest request = GetObjectTaggingRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectTaggingResponse response = s3Client.getObjectTagging(request);

        return response.tagSet().stream()
                .collect(Collectors.toMap(Tag::key, Tag::value));
    }


    // Get the current bucket policies
    public String getBucketPolicy(String bucketName) {
        try {
            GetBucketPolicyRequest request = GetBucketPolicyRequest.builder()
                    .bucket(bucketName)
                    .build();

            GetBucketPolicyResponse response = s3Client.getBucketPolicy(request);
            return response.policy();

        } catch (S3Exception e) {
            log.info("Status code: {}", e.statusCode());
            log.info("Message: {}", e.getMessage());
            if (e.statusCode() == 404) {//
                return "No bucket policy configured";
            }
            throw e; // just in case return other error code
        }

    }


}
