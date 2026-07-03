package com.jobpilot.interfaces.rest.v1.storage;

import com.jobpilot.application.storage.dto.FileUploadResponse;
import com.jobpilot.application.storage.dto.UploadFileCommand;
import com.jobpilot.application.storage.ports.FileStoragePort;
import com.jobpilot.application.storage.service.FileUploadService;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/files")
public class FileStorageController {

    private final FileUploadService fileUploadService;
    private final FileStoragePort fileStoragePort;

    public FileStorageController(FileUploadService fileUploadService, FileStoragePort fileStoragePort) {
        this.fileUploadService = fileUploadService;
        this.fileStoragePort = fileStoragePort;
    }

    @RateLimited(capacity = 100)
    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> upload(@RequestParam("file") MultipartFile file,
                                                     @RequestParam(value = "directory", required = false) String directory) {
        var response = fileUploadService.execute(new UploadFileCommand(file, directory));
        return ResponseEntity.ok(response);
    }

    @RateLimited(capacity = 100)
    @GetMapping("/{bucket}/{key}")
    public ResponseEntity<InputStreamResource> download(@PathVariable String bucket,
                                                         @PathVariable String key) throws IOException {
        var in = fileStoragePort.retrieve(bucket, key);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", key);
        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(in));
    }

    @RateLimited(capacity = 100)
    @DeleteMapping("/{bucket}/{key}")
    public ResponseEntity<Void> delete(@PathVariable String bucket, @PathVariable String key) {
        fileStoragePort.delete(bucket, key);
        return ResponseEntity.noContent().build();
    }
}
