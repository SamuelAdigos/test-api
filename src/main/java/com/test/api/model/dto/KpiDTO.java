package com.test.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
public class KpiDTO {
	private Integer totalProcessedJsonFiles;
	private Integer totalNumberOfRows;
	private Integer totalNumerOfCalls;
	private Integer totalNumberOfMessages;
	private Integer totalNumberDifferentOriginCountryCodes;
	private Integer totalDifferentDestinationCountryCodes;
	Map<String, Long> jsonProcessDuretionMillisMap;
 
	public Integer getTotalProcessedJsonFiles() {
		return totalProcessedJsonFiles;
	}

	public void setTotalProcessedJsonFiles(Integer totalProcessedJsonFiles) {
		this.totalProcessedJsonFiles = totalProcessedJsonFiles;
	}

	public Integer getTotalNumberOfRows() {
		return totalNumberOfRows;
	}

	public void setTotalNumberOfRows(Integer totalNumberOfRows) {
		this.totalNumberOfRows = totalNumberOfRows;
	}

	public Integer getTotalNumerOfCalls() {
		return totalNumerOfCalls;
	}

	public void setTotalNumerOfCalls(Integer totalNumerOfCalls) {
		this.totalNumerOfCalls = totalNumerOfCalls;
	}

	public Integer getTotalNumberOfMessages() {
		return totalNumberOfMessages;
	}

	public void setTotalNumberOfMessages(Integer totalNumberOfMessages) {
		this.totalNumberOfMessages = totalNumberOfMessages;
	}

	public Integer getTotalNumberDifferentOriginCountryCodes() {
		return totalNumberDifferentOriginCountryCodes;
	}

	public void setTotalNumberDifferentOriginCountryCodes(Integer totalNumberDifferentOriginCountryCodes) {
		this.totalNumberDifferentOriginCountryCodes = totalNumberDifferentOriginCountryCodes;
	}

	public Integer getTotalDifferentDestinationCountryCodes() {
		return totalDifferentDestinationCountryCodes;
	}

	public void setTotalDifferentDestinationCountryCodes(Integer totalDifferentDestinationCountryCodes) {
		this.totalDifferentDestinationCountryCodes = totalDifferentDestinationCountryCodes;
	}

	public Map<String, Long> getJsonProcessDuretionMillisMap() {
		return jsonProcessDuretionMillisMap;
	}

	public void setJsonProcessDuretionMillisMap(Map<String, Long> jsonProcessDuretionMillisMap) {
		this.jsonProcessDuretionMillisMap = jsonProcessDuretionMillisMap;
	}
}
