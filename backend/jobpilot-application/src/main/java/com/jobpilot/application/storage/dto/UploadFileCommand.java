package com.jobpilot.application.storage.dto;

import org.springframework.web.multipart.MultipartFile;

public record UploadFileCommand(MultipartFile file, String directory) {}
