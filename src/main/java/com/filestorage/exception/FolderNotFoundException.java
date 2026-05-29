package com.filestorage.exception;

public class FolderNotFoundException extends StorageException {

    public FolderNotFoundException(Long folderId) {
        super("Folder not found with id: " + folderId, "FOLDER_NOT_FOUND", 404);
    }
}
