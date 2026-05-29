package com.filestorage.dto;

import com.filestorage.model.AccessLevel;

public record FileUploadRequest(
        Long folderId,
        AccessLevel accessLevel
) {}
