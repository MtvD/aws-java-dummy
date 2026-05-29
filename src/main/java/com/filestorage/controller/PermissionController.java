package com.filestorage.controller;

import com.filestorage.dto.GrantPermissionRequest;
import com.filestorage.dto.PermissionResponse;
import com.filestorage.security.SecurityUtils;
import com.filestorage.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/files/{fileId}/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    public ResponseEntity<Void> grant(
            @PathVariable Long fileId,
            @Valid @RequestBody GrantPermissionRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        permissionService.grantPermission(userId, request.targetUserId(), fileId, request.permissionType());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{permissionId}")
    public ResponseEntity<Void> revoke(
            @PathVariable Long fileId,
            @PathVariable Long permissionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        permissionService.revokePermissionById(userId, permissionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<PermissionResponse>> list(@PathVariable Long fileId) {
        return ResponseEntity.ok(permissionService.listPermissions(fileId));
    }
}
