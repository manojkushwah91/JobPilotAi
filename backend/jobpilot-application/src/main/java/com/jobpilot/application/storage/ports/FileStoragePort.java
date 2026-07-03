package com.jobpilot.application.storage.ports;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileStoragePort {
    String store(String bucket, String key, InputStream data, String contentType, long size);
    String upload(MultipartFile file, String directory);
    InputStream retrieve(String bucket, String key);
    void delete(String bucket, String key);
    boolean exists(String bucket, String key);
}
