package com.filestorage.repository;

import com.filestorage.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    Optional<Folder> findByNameAndParentFolder(String name, Folder parentFolder);

    List<Folder> findByParentFolder(Folder parentFolder);

    List<Folder> findByParentFolderIsNull();
}
