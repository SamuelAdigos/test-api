package com.test.api.model.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "row_errors")
@NoArgsConstructor
@AllArgsConstructor
public class RowErrors implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "missing_fields")
	private Integer missingFields = 0;
 
	@Column(name = "messages_blank_content")
	private Integer messagesWithBlankContent = 0;

	@Column(name = "field_errors")
	private Integer fieldErrors = 0;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "file_metrics_id", referencedColumnName = "id")
	@JsonIgnore
	private FileMetrics fileMetrics;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getMissingFields() {
		return missingFields;
	}

	public void setMissingFields(Integer missingFields) {
		this.missingFields = missingFields;
	}

	public Integer getFieldErrors() {
		return fieldErrors;
	}

	public void setFieldErrors(Integer fieldErrors) {
		this.fieldErrors = fieldErrors;
	}

	public FileMetrics getFileMetrics() {
		return fileMetrics;
	}

	public void setFileMetrics(FileMetrics fileMetrics) {
		this.fileMetrics = fileMetrics;
	}

	public Integer getMessagesWithBlankContent() {
		return messagesWithBlankContent;
	}

	public void setMessagesWithBlankContent(Integer messagesWithBlankContent) {
		this.messagesWithBlankContent = messagesWithBlankContent;
	}
}
