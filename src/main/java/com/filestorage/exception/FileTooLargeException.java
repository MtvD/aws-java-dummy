package com.filestorage.exception;

public class FileTooLargeException extends StorageException {

    public FileTooLargeException(long maxSizeMb) {
        super("File size exceeds the maximum allowed size of " + maxSizeMb + "MB", "FILE_TOO_LARGE", 413);
    }
}
