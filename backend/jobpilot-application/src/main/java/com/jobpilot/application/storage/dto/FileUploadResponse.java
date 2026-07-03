package com.jobpilot.application.storage.dto;

public record FileUploadResponse(String url, String fileName, long size, String contentType) {}
