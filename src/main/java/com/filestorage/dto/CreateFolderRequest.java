package com.filestorage.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateFolderRequest(
        @NotBlank String name,
        Long parentFolderId
) {}
