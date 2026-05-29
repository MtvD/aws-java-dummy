package com.filestorage.repository;

import com.filestorage.model.FileEntity;
import com.filestorage.model.Folder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

    Optional<FileEntity> findByNameAndFolder(String name, Folder folder);

    List<FileEntity> findByFolder(Folder folder);

    List<FileEntity> findByFolderIsNull();

    Page<FileEntity> findByNameContainingIgnoreCaseAndIdIn(String name, List<Long> ids, Pageable pageable);

    long countByNameStartingWithAndFolder(String namePrefix, Folder folder);
}
