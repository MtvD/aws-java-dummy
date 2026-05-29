package com.filestorage.property;

import com.filestorage.dto.FileResponse;
import com.filestorage.model.*;
import com.filestorage.repository.*;
import com.filestorage.service.*;
import com.filestorage.service.impl.FileServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for file upload operations.
 */
class FilePropertyTest {

    // Feature: file-storage-system, Property 1: Upload creates matching record
    // Validates: Requirements 1.1, 1.4
    @Property(tries = 100)
    void uploadCreatesMatchingRecord(
            @ForAll @AlphaChars @StringLength(min = 1, max = 50) String fileName,
            @ForAll @IntRange(min = 1, max = 100) int sizeMb,
            @ForAll @AlphaChars @StringLength(min = 1, max = 30) String username,
            @ForAll @AlphaChars @StringLength(min = 1, max = 20) String contentTypeBase
    ) throws Exception {
        // Arrange
        long fileSize = (long) sizeMb * 1024 * 1024;
        String fullFileName = fileName + ".dat";
        String contentType = "application/" + contentTypeBase;

        // Set up mocks
        StorageStrategy storageStrategy = mock(StorageStrategy.class);
        FileRepository fileRepository = mock(FileRepository.class);
        FolderRepository folderRepository = mock(FolderRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        PermissionChecker permissionChecker = mock(PermissionChecker.class);
        AccessValidator accessValidator = mock(AccessValidator.class);
        AccessLogRepository accessLogRepository = mock(AccessLogRepository.class);

        FileServiceImpl fileService = new FileServiceImpl(
                storageStrategy, fileRepository, folderRepository,
                userRepository, permissionChecker, accessValidator, accessLogRepository
        );
        // Set maxSizeMb via reflection since it's a @Value field
        var maxSizeField = FileServiceImpl.class.getDeclaredField("maxSizeMb");
        maxSizeField.setAccessible(true);
        maxSizeField.setInt(fileService, 100);

        User owner = User.builder().id(1L).username(username)
                .email(username + "@test.com").passwordHash("hash").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        // No duplicate name
        when(fileRepository.findByNameAndFolder(eq(fullFileName), isNull()))
                .thenReturn(Optional.empty());

        when(storageStrategy.upload(anyString(), any(InputStream.class), eq(fileSize), eq(contentType)))
                .thenReturn("s3-key");

        // Simulate JPA save: set ID and trigger @PrePersist
        when(fileRepository.save(any(FileEntity.class))).thenAnswer(invocation -> {
            FileEntity entity = invocation.getArgument(0);
            entity.setId(42L);
            entity.onCreate(); // simulate @PrePersist
            return entity;
        });

        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getOriginalFilename()).thenReturn(fullFileName);
        when(multipartFile.getSize()).thenReturn(fileSize);
        when(multipartFile.getContentType()).thenReturn(contentType);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        // Act
        FileResponse response = fileService.upload(multipartFile, null, null, 1L);

        // Assert - Property 1: returned response matches uploaded file attributes
        assert response.id() != null : "File ID should not be null";
        assert response.name().equals(fullFileName) :
                "File name should match: expected=" + fullFileName + " actual=" + response.name();
        assert response.size() == fileSize :
                "File size should match: expected=" + fileSize + " actual=" + response.size();
        assert response.contentType().equals(contentType) :
                "Content type should match: expected=" + contentType + " actual=" + response.contentType();
        assert response.createdAt() != null : "Created timestamp should not be null";
    }
}
