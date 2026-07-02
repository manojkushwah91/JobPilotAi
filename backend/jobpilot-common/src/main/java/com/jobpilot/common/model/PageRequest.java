package com.jobpilot.common.model;

public record PageRequest(int page, int size, String sort, String direction) {
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public PageRequest {
        if (page < 0) page = DEFAULT_PAGE;
        if (size < 1 || size > MAX_SIZE) size = DEFAULT_SIZE;
        if (sort == null) sort = "createdAt";
        if (direction == null) direction = "DESC";
    }

    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size, "createdAt", "DESC");
    }
}
