package com.filestorage.service.impl;

import com.filestorage.dto.*;
import com.filestorage.exception.*;
import com.filestorage.model.*;
import com.filestorage.repository.*;
import com.filestorage.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final StorageStrategy storageStrategy;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final PermissionChecker permissionChecker;
    private final AccessValidator accessValidator;
    private final AccessLogRepository accessLogRepository;

    @Value("${file.max-size-mb}")
    private int maxSizeMb;

    private static final Duration PRESIGNED_URL_EXPIRY = Duration.ofMinutes(15);

    @Override
    @Transactional
    public FileResponse upload(MultipartFile file, Long folderId, AccessLevel accessLevel, Long userId) {
        // Validate file size
        long maxBytes = (long) maxSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new FileTooLargeException(maxSizeMb);
        }

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Folder folder = null;
        if (folderId != null) {
            folder = folderRepository.findById(folderId)
                    .orElseThrow(() -> new FolderNotFoundException(folderId));
        }

        // Handle duplicate names
        String originalName = file.getOriginalFilename();
        String storedName = resolveFileName(originalName, folder);

        // Generate S3 key
        String s3Key = UUID.randomUUID() + "/" + storedName;

        // Upload to S3
        try {
            storageStrategy.upload(s3Key, file.getInputStream(), file.getSize(), file.getContentType());
        } catch (IOException e) {
            throw new StorageBackendException("Failed to read file content", e);
        }

        // Save entity
        FileEntity entity = FileEntity.builder()
                .name(storedName)
                .originalName(originalName)
                .s3Key(s3Key)
                .size(file.getSize())
                .contentType(file.getContentType())
                .accessLevel(accessLevel != null ? accessLevel : AccessLevel.INTERNAL)
                .folder(folder)
                .owner(owner)
                .build();
        entity = fileRepository.save(entity);

        return toFileResponse(entity);
    }

    @Override
    public DownloadResponse download(Long fileId, Long userId, String clientIp) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));

        // Check permission
        if (!permissionChecker.hasPermission(userId, fileId, PermissionType.READ)) {
            throw new AccessDeniedException("You do not have permission to download this file");
        }

        // Check network access
        if (!accessValidator.isAccessAllowed(file, clientIp)) {
            throw AccessDeniedException.internalOnly();
        }

        String url = storageStrategy.generatePresignedUrl(file.getS3Key(), PRESIGNED_URL_EXPIRY);
        LocalDateTime expiresAt = LocalDateTime.now().plus(PRESIGNED_URL_EXPIRY);

        return new DownloadResponse(url, expiresAt);
    }

    @Override
    public FileResponse getFileMetadata(Long fileId, Long userId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));

        if (!permissionChecker.hasPermission(userId, fileId, PermissionType.READ)) {
            throw new AccessDeniedException("You do not have permission to view this file");
        }

        return toFileResponse(file);
    }

    @Override
    @Transactional
    public FileResponse changeAccessLevel(Long fileId, AccessLevel newLevel, Long userId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));

        if (!file.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Only the file owner can change access level");
        }

        AccessLevel previousLevel = file.getAccessLevel();
        file.setAccessLevel(newLevel);
        file = fileRepository.save(file);

        // Log the change
        User user = userRepository.findById(userId).orElseThrow();
        AccessLog log = AccessLog.builder()
                .file(file)
                .user(user)
                .previousLevel(previousLevel)
                .newLevel(newLevel)
                .build();
        accessLogRepository.save(log);

        return toFileResponse(file);
    }

    @Override
    @Transactional
    public void deleteFile(Long fileId, Long userId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));

        if (!file.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Only the file owner can delete this file");
        }

        storageStrategy.delete(file.getS3Key());
        fileRepository.delete(file);
    }

    private String resolveFileName(String originalName, Folder folder) {
        if (fileRepository.findByNameAndFolder(originalName, folder).isEmpty()) {
            return originalName;
        }

        String baseName = originalName;
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = originalName.substring(0, dotIndex);
            extension = originalName.substring(dotIndex);
        }

        int counter = 1;
        String newName;
        do {
            newName = baseName + "(" + counter + ")" + extension;
            counter++;
        } while (fileRepository.findByNameAndFolder(newName, folder).isPresent());

        return newName;
    }

    private FileResponse toFileResponse(FileEntity entity) {
        return new FileResponse(
                entity.getId(),
                entity.getName(),
                entity.getSize(),
                entity.getContentType(),
                entity.getAccessLevel(),
                entity.getFolder() != null ? entity.getFolder().getId() : null,
                entity.getCreatedAt()
        );
    }
}
