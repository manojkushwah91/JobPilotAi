package com.jobpilot.infrastructure.storage;

import com.jobpilot.application.storage.ports.FileStoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
@Profile("prod")
@Primary
public class S3FileStorageAdapter implements FileStoragePort {

    private static final Logger log = LoggerFactory.getLogger(S3FileStorageAdapter.class);

    private final LocalFileStorageAdapter fallback;

    public S3FileStorageAdapter(LocalFileStorageAdapter fallback) {
        this.fallback = fallback;
    }

    @Override
    public String store(String bucket, String key, InputStream data, String contentType, long size) {
        log.warn("S3 storage not yet configured — falling back to local storage");
        return fallback.store(bucket, key, data, contentType, size);
    }

    @Override
    public String upload(MultipartFile file, String directory) {
        log.warn("S3 storage not yet configured — falling back to local storage");
        return fallback.upload(file, directory);
    }

    @Override
    public InputStream retrieve(String bucket, String key) {
        log.warn("S3 storage not yet configured — falling back to local storage");
        return fallback.retrieve(bucket, key);
    }

    @Override
    public void delete(String bucket, String key) {
        log.warn("S3 storage not yet configured — falling back to local storage");
        fallback.delete(bucket, key);
    }

    @Override
    public boolean exists(String bucket, String key) {
        log.warn("S3 storage not yet configured — falling back to local storage");
        return fallback.exists(bucket, key);
    }
}
