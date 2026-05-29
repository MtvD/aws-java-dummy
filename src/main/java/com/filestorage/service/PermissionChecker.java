package com.filestorage.service;

import com.filestorage.model.PermissionType;

public interface PermissionChecker {

    boolean hasPermission(Long userId, Long fileId, PermissionType type);

    void grantPermission(Long ownerId, Long targetUserId, Long fileId, PermissionType type);

    void revokePermission(Long ownerId, Long targetUserId, Long fileId, PermissionType type);
}
