package com.filestorage.exception;

import lombok.Getter;

@Getter
public abstract class StorageException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    protected StorageException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    protected StorageException(String message, String errorCode, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
