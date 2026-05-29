package com.filestorage.exception;

public class FileNotFoundException extends StorageException {

    public FileNotFoundException(Long fileId) {
        super("File not found with id: " + fileId, "FILE_NOT_FOUND", 404);
    }
}
