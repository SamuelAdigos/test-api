package com.test.api.service;

import com.test.api.model.data.ProcessedFile;
import com.test.api.repository.ProcessedFiledRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProcessedFileService {

	@Autowired
	private ProcessedFiledRepository processedFiledRepository;
 
	/**
	 * Method of saving a processed file. In case it exists, the date and metrics
	 * are updated to the last ones obtained.
	 * 
	 * @param processedFile Object with the data from the processed JSON file.
	 */
	public void save(ProcessedFile processedFile) {
		ProcessedFile existingFile = processedFiledRepository.findByFileDate(processedFile.getFileDate());
		if (existingFile != null) {
			existingFile.setFileDate(processedFile.getFileDate());
			existingFile.setFileMetrics(processedFile.getFileMetrics());
			processedFiledRepository.save(existingFile);

		} else {
			processedFiledRepository.save(processedFile);
		}
	}

	public List<ProcessedFile> findAll() {
		return processedFiledRepository.findAll();
	}

	public ProcessedFile findByDate(Integer date) {
		return processedFiledRepository.findByFileDate(date);
	}
}
