package com.test.api.model.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "country_code_data")
@NoArgsConstructor
@AllArgsConstructor
public class CountryCodeData {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@Column(name = "country_code")
	private Integer countryCode;

	@Column(name = "origin_country_code")
	private Integer numberOfCallsOrigin = 0;

	@Column(name = "destination_country_code")
	private Integer numberOfCallsDestination = 0;

	@Column(name = "total_duration")
	private Integer totalDuration = 0;

	@Column(name = "average_call_duration")
	private Double averageCallDuration = 0d;
 
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "file_metrics_id", nullable = false)
	@JsonIgnore
	private FileMetrics fileMetrics;

	public Integer getTotalDuration() {
		return totalDuration;
	}

	public void setTotalDuration(Integer totalDuration) {
		this.totalDuration = totalDuration;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(Integer countryCode) {
		this.countryCode = countryCode;
	}

	public Integer getNumberOfCallsOrigin() {
		return numberOfCallsOrigin;
	}

	public void setNumberOfCallsOrigin(Integer numberOfCallsOrigin) {
		this.numberOfCallsOrigin = numberOfCallsOrigin;
	}

	public Integer getNumberOfCallsDestination() {
		return numberOfCallsDestination;
	}

	public void setNumberOfCallsDestination(Integer numberOfCallsDestination) {
		this.numberOfCallsDestination = numberOfCallsDestination;
	}

	public Double getAverageCallDuration() {
		return averageCallDuration;
	}

	public void setAverageCallDuration(Double averageCallDuration) {
		this.averageCallDuration = averageCallDuration;
	}

	public FileMetrics getFileMetrics() {
		return fileMetrics;
	}

	public void setFileMetrics(FileMetrics fileMetrics) {
		this.fileMetrics = fileMetrics;
	}

	public void increaseOriginCalls() {
		this.numberOfCallsOrigin++;
	}

	public void increaseDestinationCalls() {
		this.numberOfCallsDestination++;
	}

	public void increaseTotalDuration(Integer duration) {
		this.totalDuration += duration;
	}
}
