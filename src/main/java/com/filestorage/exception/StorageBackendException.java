package com.filestorage.exception;

public class StorageBackendException extends StorageException {

    public StorageBackendException(String message, Throwable cause) {
        super(message, "STORAGE_ERROR", 502, cause);
    }
}
