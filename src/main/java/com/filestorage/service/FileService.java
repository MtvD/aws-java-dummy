package com.filestorage.service;

import com.filestorage.dto.*;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    FileResponse upload(MultipartFile file, Long folderId, com.filestorage.model.AccessLevel accessLevel, Long userId);

    DownloadResponse download(Long fileId, Long userId, String clientIp);

    FileResponse getFileMetadata(Long fileId, Long userId);

    FileResponse changeAccessLevel(Long fileId, com.filestorage.model.AccessLevel newLevel, Long userId);

    void deleteFile(Long fileId, Long userId);
}
