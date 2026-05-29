package com.filestorage.service;

import com.filestorage.dto.FolderContentsResponse;
import com.filestorage.dto.FolderResponse;

public interface FolderService {

    FolderResponse createFolder(String name, Long parentFolderId, Long userId);

    FolderContentsResponse listContents(Long folderId, Long userId);

    FolderResponse updateFolder(Long folderId, String name, Long userId);

    void deleteFolder(Long folderId, Long userId);

    void moveFileToFolder(Long fileId, Long targetFolderId, Long userId);
}
