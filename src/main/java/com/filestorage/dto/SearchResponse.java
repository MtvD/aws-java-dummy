package com.filestorage.dto;

import java.util.List;

public record SearchResponse(
        List<FileResponse> files,
        int page,
        int totalPages,
        long totalItems
) {}
