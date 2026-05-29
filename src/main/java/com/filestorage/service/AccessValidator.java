package com.filestorage.service;

import com.filestorage.model.FileEntity;

public interface AccessValidator {

    boolean isAccessAllowed(FileEntity file, String clientIp);
}
