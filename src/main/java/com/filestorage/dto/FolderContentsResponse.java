package com.filestorage.dto;

import java.util.List;

public record FolderContentsResponse(
        List<FolderResponse> folders,
        List<FileResponse> files
) {}
