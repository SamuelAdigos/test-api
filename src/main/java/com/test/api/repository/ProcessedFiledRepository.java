package com.test.api.repository;

import com.test.api.model.data.ProcessedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedFiledRepository extends JpaRepository<ProcessedFile, Long> {
	ProcessedFile findByFileDate(Integer fileDate);
}
 