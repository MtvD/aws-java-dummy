package com.filestorage.exception;

public class InvalidCredentialsException extends StorageException {

    public InvalidCredentialsException() {
        super("Invalid username or password", "INVALID_CREDENTIALS", 401);
    }
}
