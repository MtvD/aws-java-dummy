package com.filestorage.dto;

import java.time.LocalDateTime;

public record DownloadResponse(
        String presignedUrl,
        LocalDateTime expiresAt
) {}
