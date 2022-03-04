package com.test.api.model.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "file_metrics")
@NoArgsConstructor
@AllArgsConstructor
public class FileMetrics implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
 
	@Column(name = "row_size")
	private int rowSize;

	@Column(name = "origin_country_codes")
	private int originCountryCodes;

	@Column(name = "destination_country_codes")
	private int destinationCountryCodes;

	@Column(name = "process_duration")
	private long processDurationMilliseconds;

	@Column(name = "average_success_calls")
	private Double successCallsPercentage;

	@Column(name = "number_of_calls")
	private Integer numerOfCalls;

	@Column(name = "number_of_messages")
	private Integer numerOfMessages;

	@Column(name = "number_of_origin_country_codes")
	private Integer differentOriginCountryCodes;

	@Column(name = "number_of_origin_destination_codes")
	private Integer differentDestinationCountryCodes;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "processed_file_id", referencedColumnName = "id")
	@JsonIgnore
	private ProcessedFile processedFile;

	@OneToOne(mappedBy = "fileMetrics", cascade = CascadeType.ALL)
	private RowErrors rowErrors;

	@OneToMany(mappedBy = "fileMetrics", cascade = CascadeType.ALL)
	private Set<CountryCodeData> countryCodeData;

	@OneToMany(mappedBy = "fileMetrics", cascade = CascadeType.ALL)
	private Set<WordOccurrence> wordOccurrences;

	public Double getSuccessCallsPercentage() {
		return successCallsPercentage;
	}

	public void setSuccessCallsPercentage(Double successCallsPercentage) {
		this.successCallsPercentage = successCallsPercentage;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getRowSize() {
		return rowSize;
	}

	public void setRowSize(int rowSize) {
		this.rowSize = rowSize;
	}

	public int getOriginCountryCodes() {
		return originCountryCodes;
	}

	public void setOriginCountryCodes(int originCountryCodes) {
		this.originCountryCodes = originCountryCodes;
	}

	public int getDestinationCountryCodes() {
		return destinationCountryCodes;
	}

	public void setDestinationCountryCodes(int destinationCountryCodes) {
		this.destinationCountryCodes = destinationCountryCodes;
	}

	public void setOriginAndDestinationCountryCodes(int originCountryCodes, int destinationCountryCodes) {
		this.originCountryCodes = originCountryCodes;
		this.destinationCountryCodes = destinationCountryCodes;
	}

	public long getProcessDurationMilliseconds() {
		return processDurationMilliseconds;
	}

	public void setProcessDurationMilliseconds(long processDuration) {
		this.processDurationMilliseconds = processDuration;
	}

	public ProcessedFile getProcessedFile() {
		return processedFile;
	}

	public void setProcessedFile(ProcessedFile processedFile) {
		this.processedFile = processedFile;
	}

	public RowErrors getRowErrors() {
		return rowErrors;
	}

	public void setRowErrors(RowErrors rowErrors) {
		if (this.rowErrors != null) {
			this.rowErrors.setFileMetrics(null);
		}
		rowErrors.setFileMetrics(this);
		this.rowErrors = rowErrors;
	}

	public Set<CountryCodeData> getCountryCodeData() {
		return countryCodeData;
	}

	public void setCountryCodeData(Set<CountryCodeData> countryCodeData) {
		this.countryCodeData = countryCodeData;
	}

	public Set<WordOccurrence> getWordOccurrences() {
		return wordOccurrences;
	}

	public void setWordOccurrences(Set<WordOccurrence> wordOccurrences) {
		this.wordOccurrences = wordOccurrences;
	}

	public Integer getNumerOfCalls() {
		return numerOfCalls;
	}

	public void setNumerOfCalls(Integer numerOfCalls) {
		this.numerOfCalls = numerOfCalls;
	}

	public Integer getNumerOfMessages() {
		return numerOfMessages;
	}

	public void setNumerOfMessages(Integer numerOfMessages) {
		this.numerOfMessages = numerOfMessages;
	}

	public void setNumberOfCallsAndMesssages(Integer numerOfCalls, Integer numerOfMessages) {
		this.numerOfCalls = numerOfCalls;
		this.numerOfMessages = numerOfMessages;
	}

	public Integer getDifferentOriginCountryCodes() {
		return differentOriginCountryCodes;
	}

	public void setDifferentOriginCountryCodes(Integer differentOriginCountryCodes) {
		this.differentOriginCountryCodes = differentOriginCountryCodes;
	}

	public Integer getDifferentDestinationCountryCodes() {
		return differentDestinationCountryCodes;
	}

	public void setDifferentDestinationCountryCodes(Integer differentDestinationCountryCodes) {
		this.differentDestinationCountryCodes = differentDestinationCountryCodes;
	}

	public void setDifferentOriginAndDestinationCountryCodes(int differentOriginCountryCodes,
			int differentDestinationCountryCodes) {
		this.differentOriginCountryCodes = differentOriginCountryCodes;
		this.differentDestinationCountryCodes = differentDestinationCountryCodes;
	}
}
