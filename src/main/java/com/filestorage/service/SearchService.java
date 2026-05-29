package com.filestorage.service;

import com.filestorage.dto.SearchResponse;

public interface SearchService {

    SearchResponse search(String query, int page, int size, Long userId);
}
