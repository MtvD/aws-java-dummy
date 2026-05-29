package com.filestorage.dto;

import com.filestorage.model.AccessLevel;
import java.time.LocalDateTime;

public record FileResponse(
        Long id,
        String name,
        long size,
        String contentType,
        AccessLevel accessLevel,
        Long folderId,
        LocalDateTime createdAt
) {}
