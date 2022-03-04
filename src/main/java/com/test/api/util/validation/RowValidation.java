package com.test.api.util.validation;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RowValidation {
	private boolean hasMissingFields = false;
	private boolean hasFieldErrors = false;
	private boolean hasMissingContent = false;

	public boolean hasErrors() {
		return hasFieldErrors || hasMissingFields || hasMissingContent;
	}

	public boolean hasMissingFields() {
		return hasMissingFields;
	}

	public void setHasMissingFields(boolean hasMissingFields) {
		this.hasMissingFields = hasMissingFields;
	}

	public boolean hasFieldErrors() {
		return hasFieldErrors;
	}
 
	public void setHasFieldErrors(boolean hasFieldErrors) {
		this.hasFieldErrors = hasFieldErrors;
	}

	public boolean hasMissingContent() {
		return hasMissingContent;
	}

	public void setHasMissingContent(boolean hasMissingContent) {
		this.hasMissingContent = hasMissingContent;
	}
}
