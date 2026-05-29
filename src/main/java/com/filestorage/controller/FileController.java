package com.filestorage.controller;

import com.filestorage.dto.*;
import com.filestorage.model.AccessLevel;
import com.filestorage.security.SecurityUtils;
import com.filestorage.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folderId", required = false) Long folderId,
            @RequestParam(value = "accessLevel", required = false) AccessLevel accessLevel) {
        Long userId = SecurityUtils.getCurrentUserId();
        FileResponse response = fileService.upload(file, folderId, accessLevel, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<DownloadResponse> download(
            @PathVariable Long fileId,
            HttpServletRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        String clientIp = getClientIp(request);
        return ResponseEntity.ok(fileService.download(fileId, userId, clientIp));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<FileResponse> getMetadata(@PathVariable Long fileId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(fileService.getFileMetadata(fileId, userId));
    }

    @PutMapping("/{fileId}/access-level")
    public ResponseEntity<FileResponse> changeAccessLevel(
            @PathVariable Long fileId,
            @Valid @RequestBody ChangeAccessLevelRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(fileService.changeAccessLevel(fileId, request.accessLevel(), userId));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(@PathVariable Long fileId) {
        Long userId = SecurityUtils.getCurrentUserId();
        fileService.deleteFile(fileId, userId);
        return ResponseEntity.noContent().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
