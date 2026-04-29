package com.example.aws.controller;

import com.example.aws.dto.PagedResponse;
import com.example.aws.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1/s3")
public class S3Controller {

    private final S3Service s3Service;

    @GetMapping("/bucket")
    public List<String> listBuckets() {
        return s3Service.listBuckets();
    }

    @PostMapping("/bucket/{name}")
    public String createBucket(@PathVariable String name) {
        String arn = s3Service.createBucket(name);
        return "Bucket '" + name + "' created with arn:" + arn;
    }

    @DeleteMapping("/bucket/{name}")
    public Boolean deleteBucket(@PathVariable String name) {
        return s3Service.deleteBucket(name);
    }

    @PostMapping("/bucket/{name}/upload")
    public String uploadFile(@PathVariable String name,
                             @RequestParam("file") MultipartFile file,
                             @RequestParam("key") String key) throws IOException {
        Path tempFile = Files.createTempFile("upload", "-" + file.getOriginalFilename());
        file.transferTo(tempFile);
        s3Service.uploadFile(name, key, tempFile);
        Files.delete(tempFile);
        return "File uploaded: " + key;
    }


    @GetMapping("/bucket/{name}/download/{key}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable("name") String name, @PathVariable("key") String key) throws IOException {
        byte[] content = s3Service.downloadFile(name, key);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }


    @GetMapping("/bucket/{name}/presign/{key}")
    public String generatePresignedUrl(@PathVariable("name") String name, @PathVariable("key") String key) {
        return s3Service.generatePresignedUrl(name, key);
    }

    @GetMapping("/bucket/{name}/pagesize/{max}")
    public PagedResponse listObjects(@PathVariable("name") String name, @PathVariable("max") int max, @RequestParam(value = "token", required = false) String token) {
        return s3Service.listObjects(name, token, max);
    }

    @PostMapping("/bucket/{name}/file/{key}/tag")
    void AddTagToObject(@PathVariable("name") String name, @PathVariable("key") String key, @RequestBody Map<String, String> newtags) {
        s3Service.AddTagToObject(name, key, newtags);
    }

    @PutMapping("/bucket/{name}/file/{key}/tag/{tag}")
    void removeTagToObject(@PathVariable("name") String name, @PathVariable("key") String key, @PathVariable("tag") String tag) {
        s3Service.removeTagToObject(name, key, tag);
    }

    @GetMapping("/bucket/{name}/file/{key}/tag")
    Map<String, String> getObjectTags(@PathVariable("name") String name, @PathVariable("key") String key){
        return s3Service.getObjectTags(name, key);
    }

    @GetMapping("/bucket/{name}/policy")
    String getBucketPolicy(@PathVariable("name") String name){
        return s3Service.getBucketPolicy(name);
    }
}