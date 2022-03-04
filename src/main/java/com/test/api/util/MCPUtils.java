package com.test.api.util;

import com.test.api.model.dto.CommunicationDTO;
import com.test.api.util.validation.RowValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MCPUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(MCPUtils.class);

	public static final String CALL = "CALL";
	public static final String MSG = "MSG";
	public static final String CALL_OK = "OK";
	public static final String CALL_KO = "KO";
	public static final String MSG_SEEN = "SEEN";
	public static final String MSG_DELIVERED = "DELIVERED";

	/**
	 * We check if the indicated number is a correct telephone number.
	 * 
	 * @param number Number in String format to be checked.
	 * @return Returns true if valid and false if not.
	 */
	public static boolean isValidPhoneNumber(String number) {
		final String regex = "^\\+?[0-9. ()-]{10,25}$";
		final Pattern pattern = Pattern.compile(regex);
		final Matcher matcher = pattern.matcher(number);
		return matcher.matches();
	}

	/**
	 * We obtain the country code by taking the first two digits based on the number
	 * received in String format.
	 * 
	 * @param number Number in String MSISDN format.
	 * @return Returns a String with the two digits of the country code obtained.
	 */
	public static String getCountryCode(String number) {
		try {
			long parsedNumber = Long.parseLong(number);
			return Long.toString(parsedNumber).substring(0, 2);
		} catch (Exception e) {
			LOGGER.error("Error parsing country code from number {}", number);
		}
		return null;
	}

	/**
	 * We perform the general validation of a specific row of a JSON file. Finally,
	 * depending on whether it is a call or a message, we pass other specific
	 * validations.
	 * 
	 * @param communication Parsed row of the JSON.
	 * @return Returns an object with the row validation.
	 */
	public static RowValidation getRowValidation(CommunicationDTO communication) {
		RowValidation rowValidation = new RowValidation();

		// Check if any field is missing in the row of the JSON file received as
		// parameter.
		if (communication.getMessageType() == null || communication.getTimestamp() == null
				|| communication.getOrigin() == null || communication.getDestination() == null) {
			LOGGER.error("Found at least one missing field");
			rowValidation.setHasMissingFields(true);
			// Check if any of the obtained fields (except "message_content" which we
			// count as another type of error) is empty.
		} else if (communication.getMessageType().isEmpty() || communication.getTimestamp().isEmpty()
				|| communication.getOrigin().isEmpty() || communication.getDestination().isEmpty()) {
			LOGGER.error("Found at least one blank field");
			rowValidation.setHasFieldErrors(true);
		} else {
			// Check if the "message_type" is CALL or MSG.
			if (!communication.getMessageType().equals(CALL) && !communication.getMessageType().equals(MSG)) {
				LOGGER.error("Found invalid value for message_type field {}", communication.getMessageType());
				rowValidation.setHasFieldErrors(true);
			}

			// Check if the timestamp complies with the format correctly.
			if (communication.getTimestamp() != null) {
				try {
					long time = Long.parseLong(communication.getTimestamp());
					Instant.ofEpochMilli(time);
				} catch (Exception ex) {
					LOGGER.error("Found invalid value for timestamp field {}", communication.getMessageType());
					rowValidation.setHasFieldErrors(true);
				}
			}

			// Check if the origin phone is a valid phone number.
			if (!MCPUtils.isValidPhoneNumber(communication.getOrigin())
					&& MCPUtils.getCountryCode(communication.getOrigin()) == null) {
				LOGGER.error("Found invalid value for origin field {}", communication.getMessageType());
				rowValidation.setHasFieldErrors(true);
			}

			// Check if the destination phone is a valid phone number.
			if (!MCPUtils.isValidPhoneNumber(communication.getDestination())
					&& MCPUtils.getCountryCode(communication.getDestination()) == null) {
				LOGGER.error("Found invalid value for origin field {}", communication.getMessageType());
				rowValidation.setHasFieldErrors(true);
			}

			switch (communication.getMessageType()) {
			case CALL:
				validateCallFields(communication, rowValidation);
				break;
			case MSG:
				validateMessageFields(communication, rowValidation);
				break;
			}
		}
		return rowValidation;
	}

	/**
	 * Specific validation for rows of type "CALL". We check the requirements
	 * specified in the test.
	 * 
	 * @param communicationDTO Parsed row of the JSON.
	 * @param rowValidation    Object with the row validation.
	 */
	private static void validateCallFields(CommunicationDTO communicationDTO, RowValidation rowValidation) {
		String duration = communicationDTO.getDuration();
		String statusCode = communicationDTO.getStatusCode();
		String statusDescription = communicationDTO.getStatusDescription();

		// Check if any of the call-specific fields are missing.
		if (duration == null || statusCode == null || statusDescription == null) {
			LOGGER.error("Found at least one missing call field");
			rowValidation.setHasMissingFields(true);
			// Check if any of the call-specific fields are empty.
		} else if (duration.isEmpty() || statusCode.isEmpty() || statusDescription.isEmpty()) {
			LOGGER.error("Found at least one blank call field");
			rowValidation.setHasFieldErrors(true);
		} else {

			// Check that the duration complies with the format correctly.
			try {
				Integer.parseInt(duration);
			} catch (NumberFormatException e) {
				LOGGER.error("Found invalid value for duration field {}", duration);
				rowValidation.setHasFieldErrors(true);
			}

			// We check that the status file meets the requirements.
			if (!statusCode.equals(CALL_OK) && !statusCode.equals(CALL_KO)) {
				LOGGER.error("Found invalid value for status_code field {}", statusCode);
				rowValidation.setHasFieldErrors(true);
			}

		}

	}

	/**
	 * Specific validation for rows of type "MSG". We check the requirements
	 * specified in the test.
	 * 
	 * @param communicationDTO Parsed row of the JSON.
	 * @param rowValidation    Object with the row validation.
	 */
	private static void validateMessageFields(CommunicationDTO communicationDTO, RowValidation rowValidation) {
		String messageContent = communicationDTO.getMessageContent();
		String messageStatus = communicationDTO.getMessageStatus();

		// Check if any of the message-specific fields are missing.
		if (messageContent == null || messageStatus == null) {
			LOGGER.error("Found at least one missing message field");
			rowValidation.setHasMissingFields(true);
			// Check if message status field are empty and if it is, we write it down as an
			// error since it is a mandatory field.
		} else if (messageStatus.isEmpty()) {
			LOGGER.error("Found at least one blank message field");
			rowValidation.setHasFieldErrors(true);
		} else {
			// In case the "message_content" field is empty, it will be written to a
			// separate counter specified in the task.
			if (messageContent.isEmpty()) {
				rowValidation.setHasMissingContent(true);
			}

			// We check if any of the values in the "message_status" field is not one of
			// those specified in the task.
			if (!messageStatus.equals(MSG_DELIVERED) && !messageStatus.equals(MSG_SEEN)) {
				LOGGER.error("Found invalid value for message_status field {}", messageStatus);
				rowValidation.setHasFieldErrors(true);
			}
		}

	}
 
	/**
	 * We obtain a ratio of successful calls based on the total number of calls.
	 * 
	 * @param okCalls    Number of OK calls.
	 * @param totalCalls Number of total calls.
	 * @return A ratio value of successful calls.
	 */
	public static double findSuccessCallRatio(int okCalls, int totalCalls) {
		final DecimalFormat df = new DecimalFormat("0,00");
		double ratio = (double) okCalls / totalCalls;
		String formattedRatio = df.format(ratio * 100);
		return Double.parseDouble(formattedRatio);
	}

	/**
	 * We obtain an average call duration based on the total number of calls in
	 * minutes.
	 * 
	 * @param totalCalls    Number of total calls.
	 * @param totalDuration Number of total duration in seconds.
	 * @return Minutes of average duration.
	 */
	public static double getAverageDuration(int totalCalls, int totalDuration) {
		final DecimalFormat df = new DecimalFormat("0,00");
		double average = (double) totalDuration / totalCalls;
		String formattedAverage = df.format(average);
		return Double.parseDouble(formattedAverage);
	}
}
