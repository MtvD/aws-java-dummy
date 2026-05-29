package com.filestorage.service.impl;

import com.filestorage.dto.FileResponse;
import com.filestorage.dto.SearchResponse;
import com.filestorage.model.FileEntity;
import com.filestorage.model.PermissionType;
import com.filestorage.repository.FileRepository;
import com.filestorage.repository.PermissionRepository;
import com.filestorage.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final FileRepository fileRepository;
    private final PermissionRepository permissionRepository;

    @Value("${search.default-page-size}")
    private int defaultPageSize;

    @Override
    public SearchResponse search(String query, int page, int size, Long userId) {
        int pageSize = size > 0 ? size : defaultPageSize;
        Pageable pageable = PageRequest.of(page, pageSize);

        // Get file IDs the user has READ permission for
        List<Long> readableFileIds = permissionRepository.findFileIdsByUserIdAndType(userId, PermissionType.READ);

        // Also include files owned by the user
        List<Long> ownedFileIds = fileRepository.findAll().stream()
                .filter(f -> f.getOwner().getId().equals(userId))
                .map(FileEntity::getId)
                .toList();

        List<Long> accessibleIds = new ArrayList<>(readableFileIds);
        accessibleIds.addAll(ownedFileIds);
        accessibleIds = accessibleIds.stream().distinct().toList();

        if (accessibleIds.isEmpty()) {
            return new SearchResponse(List.of(), page, 0, 0);
        }

        Page<FileEntity> results = fileRepository.findByNameContainingIgnoreCaseAndIdIn(
                query, accessibleIds, pageable);

        List<FileResponse> files = results.getContent().stream()
                .map(this::toFileResponse)
                .toList();

        return new SearchResponse(files, page, results.getTotalPages(), results.getTotalElements());
    }

    private FileResponse toFileResponse(FileEntity entity) {
        return new FileResponse(
                entity.getId(),
                entity.getName(),
                entity.getSize(),
                entity.getContentType(),
                entity.getAccessLevel(),
                entity.getFolder() != null ? entity.getFolder().getId() : null,
                entity.getCreatedAt()
        );
    }
}
