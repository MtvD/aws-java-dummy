package com.filestorage.dto;

import com.filestorage.model.PermissionType;
import java.time.LocalDateTime;

public record PermissionResponse(
        Long id,
        Long userId,
        String username,
        PermissionType type,
        LocalDateTime grantedAt
) {}
