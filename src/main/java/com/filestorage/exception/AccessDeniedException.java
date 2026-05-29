package com.filestorage.exception;

public class AccessDeniedException extends StorageException {

    public AccessDeniedException(String message) {
        super(message, "ACCESS_DENIED", 403);
    }

    public static AccessDeniedException internalOnly() {
        return new AccessDeniedException("This file is restricted to internal network access only") {
            @Override
            public String getErrorCode() {
                return "INTERNAL_ONLY";
            }
        };
    }
}
