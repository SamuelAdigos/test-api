package com.test.api.model.data;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "processed_files")
public class ProcessedFile implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
 
	@Column(name = "file_date", unique = true)
	private Integer fileDate;

	@OneToOne(mappedBy = "processedFile", cascade = CascadeType.ALL)
	private FileMetrics fileMetrics;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getFileDate() {
		return fileDate;
	}

	public void setFileDate(Integer fileDate) {
		this.fileDate = fileDate;
	}

	public FileMetrics getFileMetrics() {
		return fileMetrics;
	}

	public void setFileMetrics(FileMetrics fileMetrics) {
		if (this.fileMetrics != null) {
			this.fileMetrics.setProcessedFile(null);
		}
		fileMetrics.setProcessedFile(this);
		this.fileMetrics = fileMetrics;
	}
}
