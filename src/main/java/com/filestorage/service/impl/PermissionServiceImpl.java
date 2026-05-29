package com.filestorage.service.impl;

import com.filestorage.dto.PermissionResponse;
import com.filestorage.exception.AccessDeniedException;
import com.filestorage.exception.FileNotFoundException;
import com.filestorage.model.*;
import com.filestorage.repository.FileRepository;
import com.filestorage.repository.PermissionRepository;
import com.filestorage.repository.UserRepository;
import com.filestorage.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    @Override
    public boolean hasPermission(Long userId, Long fileId, PermissionType type) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));
        // Owner has all permissions
        if (file.getOwner().getId().equals(userId)) {
            return true;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;
        return permissionRepository.existsByUserAndFileAndType(user, file, type);
    }

    @Override
    @Transactional
    public void grantPermission(Long ownerId, Long targetUserId, Long fileId, PermissionType type) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));
        validateOwner(file, ownerId);

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Target user not found"));
        User owner = userRepository.findById(ownerId).orElseThrow();

        // Check if permission already exists
        if (permissionRepository.existsByUserAndFileAndType(targetUser, file, type)) {
            return; // Already granted
        }

        Permission permission = Permission.builder()
                .user(targetUser)
                .file(file)
                .type(type)
                .grantedBy(owner)
                .build();
        permissionRepository.save(permission);
    }

    @Override
    @Transactional
    public void revokePermission(Long ownerId, Long targetUserId, Long fileId, PermissionType type) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));
        validateOwner(file, ownerId);

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Target user not found"));

        permissionRepository.findByUserAndFileAndType(targetUser, file, type)
                .ifPresent(permissionRepository::delete);
    }

    @Override
    @Transactional
    public void revokePermissionById(Long ownerId, Long permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found"));
        validateOwner(permission.getFile(), ownerId);
        permissionRepository.delete(permission);
    }

    @Override
    public List<PermissionResponse> listPermissions(Long fileId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));
        return permissionRepository.findByFile(file).stream()
                .map(p -> new PermissionResponse(
                        p.getId(),
                        p.getUser().getId(),
                        p.getUser().getUsername(),
                        p.getType(),
                        p.getGrantedAt()
                ))
                .toList();
    }

    private void validateOwner(FileEntity file, Long userId) {
        if (!file.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Only the file owner can modify permissions");
        }
    }
}
