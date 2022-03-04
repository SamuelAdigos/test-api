package com.test.api.model.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
public class ProcessResponseDTO {
	private String message;
 
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
