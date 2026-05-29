package com.filestorage.dto;

import java.time.LocalDateTime;

public record FolderResponse(
        Long id,
        String name,
        Long parentFolderId,
        LocalDateTime createdAt
) {}
