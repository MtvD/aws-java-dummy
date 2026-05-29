package com.filestorage.dto;

import com.filestorage.model.PermissionType;
import jakarta.validation.constraints.NotNull;

public record GrantPermissionRequest(
        @NotNull Long targetUserId,
        @NotNull PermissionType permissionType
) {}
