package com.jobpilot.application.storage.service;

import com.jobpilot.application.storage.dto.FileUploadResponse;
import com.jobpilot.application.storage.dto.UploadFileCommand;
import com.jobpilot.application.storage.ports.FileStoragePort;
import com.jobpilot.common.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class FileUploadService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "image/jpeg", "image/png");

    private final FileStoragePort fileStoragePort;

    public FileUploadService(FileStoragePort fileStoragePort) {
        this.fileStoragePort = fileStoragePort;
    }

    public FileUploadResponse execute(UploadFileCommand command) {
        var file = command.file();
        var contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new ValidationException("fileType", "Invalid file type. Allowed: PDF, DOCX, JPG, PNG");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException("fileSize", "File size must not exceed 10MB");
        }
        var url = fileStoragePort.upload(file, command.directory());
        return new FileUploadResponse(url, file.getOriginalFilename(), file.getSize(), contentType);
    }
}
