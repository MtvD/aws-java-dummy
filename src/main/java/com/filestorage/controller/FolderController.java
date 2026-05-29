package com.filestorage.controller;

import com.filestorage.dto.*;
import com.filestorage.security.SecurityUtils;
import com.filestorage.service.FolderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @PostMapping
    public ResponseEntity<FolderResponse> create(@Valid @RequestBody CreateFolderRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        FolderResponse response = folderService.createFolder(request.name(), request.parentFolderId(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{folderId}/contents")
    public ResponseEntity<FolderContentsResponse> listContents(@PathVariable Long folderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(folderService.listContents(folderId, userId));
    }

    @PutMapping("/{folderId}")
    public ResponseEntity<FolderResponse> update(
            @PathVariable Long folderId,
            @Valid @RequestBody CreateFolderRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(folderService.updateFolder(folderId, request.name(), userId));
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> delete(@PathVariable Long folderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        folderService.deleteFolder(folderId, userId);
        return ResponseEntity.noContent().build();
    }
}
