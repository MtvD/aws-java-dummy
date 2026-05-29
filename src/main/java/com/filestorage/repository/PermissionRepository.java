package com.filestorage.repository;

import com.filestorage.model.FileEntity;
import com.filestorage.model.Permission;
import com.filestorage.model.PermissionType;
import com.filestorage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByUserAndFileAndType(User user, FileEntity file, PermissionType type);

    List<Permission> findByFile(FileEntity file);

    List<Permission> findByUserAndFile(User user, FileEntity file);

    @Query("SELECT p.file.id FROM Permission p WHERE p.user.id = :userId AND p.type = :type")
    List<Long> findFileIdsByUserIdAndType(@Param("userId") Long userId, @Param("type") PermissionType type);

    boolean existsByUserAndFileAndType(User user, FileEntity file, PermissionType type);
}
