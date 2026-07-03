package com.jobpilot.infrastructure.storage;

import com.jobpilot.application.storage.ports.FileStoragePort;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class LocalFileStorageAdapter implements FileStoragePort {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageAdapter.class);

    private final Path basePath;

    public LocalFileStorageAdapter(@Value("${storage.local.path:./uploads}") String basePath) {
        this.basePath = Paths.get(basePath).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(basePath);
        log.info("Local file storage initialized at {}", basePath);
    }

    @Override
    public String store(String bucket, String key, InputStream data, String contentType, long size) {
        try {
            var bucketDir = basePath.resolve(bucket);
            Files.createDirectories(bucketDir);
            var target = bucketDir.resolve(key).normalize();
            if (!target.startsWith(basePath)) {
                throw new SecurityException("Path traversal detected: " + key);
            }
            Files.copy(data, target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file: " + key, e);
        }
    }

    @Override
    public String upload(MultipartFile file, String directory) {
        var key = UUID.randomUUID() + "_" + file.getOriginalFilename();
        try (var in = file.getInputStream()) {
            return store(directory, key, in, file.getContentType(), file.getSize());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to upload file: " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public InputStream retrieve(String bucket, String key) {
        try {
            var target = basePath.resolve(bucket).resolve(key).normalize();
            if (!target.startsWith(basePath) || !Files.exists(target)) {
                throw new FileNotFoundException("File not found: " + bucket + "/" + key);
            }
            return Files.newInputStream(target);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to retrieve file: " + key, e);
        }
    }

    @Override
    public void delete(String bucket, String key) {
        try {
            var target = basePath.resolve(bucket).resolve(key).normalize();
            if (target.startsWith(basePath)) {
                Files.deleteIfExists(target);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete file: " + key, e);
        }
    }

    @Override
    public boolean exists(String bucket, String key) {
        var target = basePath.resolve(bucket).resolve(key).normalize();
        return target.startsWith(basePath) && Files.exists(target);
    }
}
