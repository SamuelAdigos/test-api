package com.test.api.model.dto;

import lombok.*;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
public class MetricsDTO {
    private Integer rowsWithMissingFields;
    private Integer messageWithBlankContent;
    private Integer rowsWithFieldErrors;
    private Set<CountryCodeDataDTO> countryCodeData;
    private Double succededFailedCallsRatio;
    private Set<WordOccurrenceDTO> wordOccurrences;

    public Integer getRowsWithMissingFields() {
        return rowsWithMissingFields;
    }

    public void setRowsWithMissingFields(Integer rowsWithMissingFields) {
        this.rowsWithMissingFields = rowsWithMissingFields;
    }

    public Integer getMessageWithBlankContent() {
        return messageWithBlankContent;
    }

    public void setMessageWithBlankContent(Integer messageWithBlankContent) {
        this.messageWithBlankContent = messageWithBlankContent;
    }

    public Integer getRowsWithFieldErrors() {
        return rowsWithFieldErrors;
    }

    public void setRowsWithFieldErrors(Integer rowsWithFieldErrors) {
        this.rowsWithFieldErrors = rowsWithFieldErrors;
    }

    public Set<CountryCodeDataDTO> getCountryCodeData() {
        return countryCodeData;
    }

    public void setCountryCodeData(Set<CountryCodeDataDTO> countryCodeData) {
        this.countryCodeData = countryCodeData;
    }

    public Double getSuccededFailedCallsRatio() {
        return succededFailedCallsRatio;
    }

    public void setSuccededFailedCallsRatio(Double succededFailedCallsRatio) {
        this.succededFailedCallsRatio = succededFailedCallsRatio;
    }

    public Set<WordOccurrenceDTO> getWordOccurrences() {
        return wordOccurrences;
    }

    public void setWordOccurrences(Set<WordOccurrenceDTO> wordOccurrences) {
        this.wordOccurrences = wordOccurrences;
    }
}
