package com.test.api.model.dto;

import com.test.api.model.data.CountryCodeData;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class CountryCodeDataDTO {
    private Integer countryCode;
    private Integer numberOfCallsOrigin;
    private Integer numberOfCallsDestination;
    private Double averageCallDuration;

    public static CountryCodeDataDTO toCountryCodeDataDTO(CountryCodeData countryCodeData) {
        if (countryCodeData == null) {
            return null;
        }
        CountryCodeDataDTO countryCodeDataDTO = new CountryCodeDataDTO();
        countryCodeDataDTO.setCountryCode(countryCodeData.getCountryCode());
        countryCodeDataDTO.setAverageCallDuration(countryCodeData.getAverageCallDuration());
        countryCodeDataDTO.setNumberOfCallsDestination(countryCodeData.getNumberOfCallsDestination());
        countryCodeDataDTO.setNumberOfCallsOrigin(countryCodeData.getNumberOfCallsOrigin());

        return countryCodeDataDTO;
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
}
