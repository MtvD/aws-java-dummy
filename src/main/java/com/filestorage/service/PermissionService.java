package com.filestorage.service;

import com.filestorage.dto.PermissionResponse;
import com.filestorage.model.PermissionType;

import java.util.List;

public interface PermissionService extends PermissionChecker {

    List<PermissionResponse> listPermissions(Long fileId);

    void revokePermissionById(Long ownerId, Long permissionId);
}
