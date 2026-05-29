package com.filestorage.exception;

public class DuplicateFolderException extends StorageException {

    public DuplicateFolderException(String folderName) {
        super("A folder with name '" + folderName + "' already exists in this location", "DUPLICATE_FOLDER", 409);
    }
}
