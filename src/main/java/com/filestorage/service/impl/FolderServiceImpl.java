package com.filestorage.service.impl;

import com.filestorage.dto.*;
import com.filestorage.exception.*;
import com.filestorage.model.*;
import com.filestorage.repository.*;
import com.filestorage.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public FolderResponse createFolder(String name, Long parentFolderId, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Folder parentFolder = null;
        if (parentFolderId != null) {
            parentFolder = folderRepository.findById(parentFolderId)
                    .orElseThrow(() -> new FolderNotFoundException(parentFolderId));
        }

        // Check duplicate name
        if (folderRepository.findByNameAndParentFolder(name, parentFolder).isPresent()) {
            throw new DuplicateFolderException(name);
        }

        Folder folder = Folder.builder()
                .name(name)
                .parentFolder(parentFolder)
                .owner(owner)
                .build();
        folder = folderRepository.save(folder);

        return toFolderResponse(folder);
    }

    @Override
    public FolderContentsResponse listContents(Long folderId, Long userId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new FolderNotFoundException(folderId));

        List<FolderResponse> subFolders = folderRepository.findByParentFolder(folder).stream()
                .map(this::toFolderResponse)
                .toList();

        List<FileResponse> files = fileRepository.findByFolder(folder).stream()
                .map(this::toFileResponse)
                .toList();

        return new FolderContentsResponse(subFolders, files);
    }

    @Override
    @Transactional
    public FolderResponse updateFolder(Long folderId, String name, Long userId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new FolderNotFoundException(folderId));

        if (!folder.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Only the folder owner can update this folder");
        }

        // Check duplicate name in same parent
        if (folderRepository.findByNameAndParentFolder(name, folder.getParentFolder()).isPresent()) {
            throw new DuplicateFolderException(name);
        }

        folder.setName(name);
        folder = folderRepository.save(folder);
        return toFolderResponse(folder);
    }

    @Override
    @Transactional
    public void deleteFolder(Long folderId, Long userId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new FolderNotFoundException(folderId));

        if (!folder.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Only the folder owner can delete this folder");
        }

        Folder parentFolder = folder.getParentFolder();

        // Move sub-folders to parent
        List<Folder> subFolders = folderRepository.findByParentFolder(folder);
        for (Folder sub : subFolders) {
            sub.setParentFolder(parentFolder);
            folderRepository.save(sub);
        }

        // Move files to parent
        List<FileEntity> files = fileRepository.findByFolder(folder);
        for (FileEntity file : files) {
            file.setFolder(parentFolder);
            fileRepository.save(file);
        }

        folderRepository.delete(folder);
    }

    @Override
    @Transactional
    public void moveFileToFolder(Long fileId, Long targetFolderId, Long userId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));

        if (!file.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Only the file owner can move this file");
        }

        Folder targetFolder = null;
        if (targetFolderId != null) {
            targetFolder = folderRepository.findById(targetFolderId)
                    .orElseThrow(() -> new FolderNotFoundException(targetFolderId));
        }

        file.setFolder(targetFolder);
        fileRepository.save(file);
    }

    private FolderResponse toFolderResponse(Folder folder) {
        return new FolderResponse(
                folder.getId(),
                folder.getName(),
                folder.getParentFolder() != null ? folder.getParentFolder().getId() : null,
                folder.getCreatedAt()
        );
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
