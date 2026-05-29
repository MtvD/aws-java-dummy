package com.filestorage.service;

import java.io.InputStream;
import java.time.Duration;

public interface StorageStrategy {

    String upload(String key, InputStream content, long size, String contentType);

    InputStream download(String key);

    String generatePresignedUrl(String key, Duration expiration);

    void delete(String key);
}
