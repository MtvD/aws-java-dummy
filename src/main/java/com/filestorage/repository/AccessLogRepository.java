package com.filestorage.repository;

import com.filestorage.model.AccessLog;
import com.filestorage.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {

    List<AccessLog> findByFileOrderByChangedAtDesc(FileEntity file);
}
