package com.test.api.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonStreamParser;
import com.test.api.model.data.*;
import com.test.api.model.dto.*;
import com.test.api.util.MCPUtils;
import com.test.api.util.validation.RowValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

	@Value("${mcp.json-url}")
	private String apiPath;

	@Value("${word-list}")
	private List<String> wordList;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ProcessedFileService processedFileService;

	/**
	 * Through a number that corresponds to the date in YYYYYYMMDD format, the
	 * corresponding JSON file is obtained through the URL specified in
	 * application.yml and the metrics of this file are collected.
	 * 
	 * @param date Date in YYYYYYMMDD number format
	 * @return "PROCESSED" response if the file was found and processed and a "NO
	 *         FILE TO PROCESS" response if an empty file was found.
	 */
	public ProcessResponseDTO processFile(Integer date) {
		ProcessResponseDTO processResponseDTO = new ProcessResponseDTO();
		List<CommunicationDTO> communicationList = new ArrayList<>();

		Instant start = Instant.now();
		String jsonFileString = restTemplate.getForObject(apiPath + "MCP_" + date + ".json", String.class);
		if (jsonFileString != null && !jsonFileString.isEmpty()) {
			Gson gson = new GsonBuilder().create();
			JsonStreamParser parser = new JsonStreamParser(jsonFileString);
			int rowErrors = 0;
			try {
				while (parser.hasNext()) {
					CommunicationDTO communicationDTO = gson.fromJson(parser.next(), CommunicationDTO.class);
					communicationList.add(communicationDTO);
				}
			} catch (Exception ex) {
				LOGGER.error("Unexpected error parsing json", ex);
				rowErrors++;
			}

			ProcessedFile processedFile = new ProcessedFile();
			processedFile.setFileDate(date);

			processedFile.setFileMetrics(getFileMetrics(communicationList, start, processedFile, rowErrors));

			processedFileService.save(processedFile);

			processResponseDTO.setMessage("PROCESSED");
		} else {
			processResponseDTO.setMessage("NO FILE TO PROCESS");
		}

		return processResponseDTO;
	}

	/**
	 * The errors per row are obtained and the object containing this information is
	 * mapped. Rows that contain a field that does not meet the format and
	 * requirements of the task, that are blank or that have JSON malformation
	 * errors are counted as errors.
	 * 
	 * @param rowValidationList      List of rows to validate.
	 * @param fileMetrics            Object with metrics.
	 * @param malformedJsonRowErrors Number of malformed JSON errors.
	 * @return Object containing all errors, empty fields and blank
	 *         "message_content".
	 */
	private RowErrors getRowErrors(List<RowValidation> rowValidationList, FileMetrics fileMetrics,
			Integer malformedJsonRowErrors) {
		int fieldErrors = (int) rowValidationList.stream().filter(RowValidation::hasFieldErrors).count();
		int missingFields = (int) rowValidationList.stream().filter(RowValidation::hasMissingFields).count();
		int messageBlankContent = (int) rowValidationList.stream().filter(RowValidation::hasMissingContent).count();

		RowErrors rowErrors = new RowErrors();
		rowErrors.setFieldErrors(fieldErrors + malformedJsonRowErrors);
		rowErrors.setMissingFields(missingFields);
		rowErrors.setFileMetrics(fileMetrics);
		rowErrors.setMessagesWithBlankContent(messageBlankContent);

		return rowErrors;
	}

	/**
	 * This method receives a list of parsed rows from a given JSON file and maps an
	 * object to the metrics specified in the task requirements.
	 * 
	 * @param communicationList List with the rows of the JSON file parsed into an
	 *                          object.
	 * @param start             Time at which the process was initiated.
	 * @param processedFile     Base object for processed files
	 * @param jsonRowErrors     Number of row errors.
	 * @return Object with the metrics of the specified JSON file.
	 */
	private FileMetrics getFileMetrics(List<CommunicationDTO> communicationList, Instant start,
			ProcessedFile processedFile, int jsonRowErrors) {
		FileMetrics fileMetrics = new FileMetrics();
		List<RowValidation> rowValidationList = new ArrayList<>();
		Map<String, CountryCodeData> countryCodeMap = new HashMap<>();
		Set<Integer> originCountryCodeSet = new HashSet<>();
		Set<Integer> destinationCountryCodeSet = new HashSet<>();
		Map<String, Integer> wordCountMap = new HashMap<>();
		int okCalls = 0;
		int totalFileCalls = 0;
		int totalFileMessages = 0;
 
		// We create a map with the keywords to be considered in the task requirements.
		wordList.forEach(word -> wordCountMap.put(word, 0));

		// We traverse all the rows of the parsed JSON file to generate its metrics.
		for (CommunicationDTO communication : communicationList) {
			RowValidation rowValidation = MCPUtils.getRowValidation(communication);
			rowValidationList.add(rowValidation);

			if (!rowValidation.hasErrors()) {
				String originCountryCode = MCPUtils.getCountryCode(communication.getOrigin());
				String destinationCountryCode = MCPUtils.getCountryCode(communication.getDestination());

				switch (communication.getMessageType()) {
				// In the case of a call, the originating and terminating calls are counted by
				// country code and their duration.
				case MCPUtils.CALL:
					totalFileCalls++;
					if (originCountryCode != null) {
						originCountryCodeSet.add(Integer.parseInt(originCountryCode));
						CountryCodeData existingCountryCodeData = countryCodeMap.get(originCountryCode);
						if (existingCountryCodeData != null) {
							existingCountryCodeData.increaseOriginCalls();
							existingCountryCodeData
									.increaseTotalDuration(Integer.parseInt(communication.getDuration()));
						} else {
							CountryCodeData countryCodeData = getCountryCodeData(fileMetrics, communication,
									originCountryCode);
							countryCodeMap.put(String.valueOf(countryCodeData.getCountryCode()), countryCodeData);
						}
					}

					if (destinationCountryCode != null) {
						destinationCountryCodeSet.add(Integer.parseInt(destinationCountryCode));
						CountryCodeData existingCountryCodeData = countryCodeMap.get(destinationCountryCode);
						if (existingCountryCodeData != null) {
							existingCountryCodeData.increaseDestinationCalls();
							existingCountryCodeData
									.increaseTotalDuration(Integer.parseInt(communication.getDuration()));
						} else {
							CountryCodeData countryCodeData = getCountryCodeData(fileMetrics, communication,
									destinationCountryCode);
							countryCodeMap.put(String.valueOf(countryCodeData.getCountryCode()), countryCodeData);
						}
					}

					// We also count the call as OK if it has this status in "status_code".
					if (communication.getStatusCode().equals(MCPUtils.CALL_OK)) {
						okCalls++;
					}
					break;

				// In the case of a message, we check if the message contains any of the words
				// marked as a requirement to be counted in the task.
				case MCPUtils.MSG:
					totalFileMessages++;
					for (String word : wordList) {
						if (communication.getMessageContent().contains(word)) {
							Integer occurrences = wordCountMap.get(word);
							occurrences++;
							wordCountMap.put(word, occurrences);
						}
					}
					break;
				}
			}
		}

		// We also count the total calls and execution duration of the JSON files to
		// make the KPIS endpoint.
		countryCodeMap.values().forEach(countryCodeData -> {
			int totalCalls = countryCodeData.getNumberOfCallsDestination() + countryCodeData.getNumberOfCallsOrigin();
			countryCodeData.setAverageCallDuration(
					MCPUtils.getAverageDuration(totalCalls, countryCodeData.getTotalDuration()));
		});

		// We store the counters of each word found in the JSON file along with its
		// metrics in an object.
		Set<WordOccurrence> wordOccurrenceSet = wordCountMap.entrySet().stream().map(e -> {
			WordOccurrence wordOccurrence = new WordOccurrence();
			wordOccurrence.setWord(e.getKey());
			wordOccurrence.setCount(e.getValue());
			wordOccurrence.setFileMetrics(fileMetrics);
			return wordOccurrence;
		}).collect(Collectors.toSet());

		Instant finish = Instant.now();
		long timeElapsed = Duration.between(start, finish).toMillis();

		// Store all the metrics obtained in the method in our metrics object for the
		// processed JSON file.
		fileMetrics.setProcessDurationMilliseconds(timeElapsed);
		fileMetrics.setProcessedFile(processedFile);
		fileMetrics.setOriginAndDestinationCountryCodes(originCountryCodeSet.size(), destinationCountryCodeSet.size());
		fileMetrics.setRowSize(communicationList.size() + jsonRowErrors);
		fileMetrics.setDifferentOriginAndDestinationCountryCodes(originCountryCodeSet.size(),
				destinationCountryCodeSet.size());
		fileMetrics.setSuccessCallsPercentage(MCPUtils.findSuccessCallRatio(okCalls, totalFileCalls));
		fileMetrics.setNumberOfCallsAndMesssages(totalFileCalls, totalFileMessages);
		fileMetrics.setRowErrors(getRowErrors(rowValidationList, fileMetrics, jsonRowErrors));
		fileMetrics.setWordOccurrences(wordOccurrenceSet);
		fileMetrics.setCountryCodeData(new HashSet<>(countryCodeMap.values()));

		return fileMetrics;
	}

	// We create an object that stores the data for each country code from the
	// beginning with the row specified in the attributes of the method.
	private CountryCodeData getCountryCodeData(FileMetrics fileMetrics, CommunicationDTO communication,
			String originCountryCode) {
		CountryCodeData countryCodeData = new CountryCodeData();
		countryCodeData.setCountryCode(Integer.parseInt(originCountryCode));
		countryCodeData.setFileMetrics(fileMetrics);
		countryCodeData.increaseOriginCalls();
		countryCodeData.increaseTotalDuration(Integer.parseInt(communication.getDuration()));
		return countryCodeData;
	}

	/**
	 * We obtain objects from JSON files already processed from the DB.
	 * 
	 * @return List of processed files
	 */
	public List<ProcessedFile> getFiles() {
		return processedFileService.findAll();
	}

	/**
	 * We obtain the previously processed metrics from a given JSON file stored in
	 * the DB through the date in YYYYMMDD format.
	 * 
	 * @param date Integer of date in YYYYMMDD format.
	 * @return Mapped metrics object.
	 */
	public MetricsDTO getMetrics(Integer date) {
		MetricsDTO metricsDTO = new MetricsDTO();

		ProcessedFile processedFile = processedFileService.findByDate(date);

		if (processedFile != null) {
			Set<CountryCodeDataDTO> countryCodeDataDTOSet = processedFile.getFileMetrics().getCountryCodeData().stream()
					.map(CountryCodeDataDTO::toCountryCodeDataDTO).collect(Collectors.toSet());

			metricsDTO.setCountryCodeData(countryCodeDataDTOSet);

			Set<WordOccurrenceDTO> wordOccurrenceDTOSet = processedFile.getFileMetrics().getWordOccurrences().stream()
					.map(WordOccurrenceDTO::toWordOccuranceDTO).collect(Collectors.toSet());

			metricsDTO.setWordOccurrences(wordOccurrenceDTOSet);

			RowErrors rowErrors = processedFile.getFileMetrics().getRowErrors();
			metricsDTO.setRowsWithFieldErrors(rowErrors.getFieldErrors());
			metricsDTO.setMessageWithBlankContent(rowErrors.getMessagesWithBlankContent());
			metricsDTO.setRowsWithMissingFields(rowErrors.getMissingFields());
			metricsDTO.setSuccededFailedCallsRatio(processedFile.getFileMetrics().getSuccessCallsPercentage());
		}

		return metricsDTO;
	}

	/**
	 * We generate the KPIS statistics specified in the task requirements through
	 * the list of all JSON files processed and subsequently stored in the DB.
	 * 
	 * @return An object with mapped KPIS statistics is returned.
	 */
	public KpiDTO getKpis() {
		KpiDTO kpiDTO = new KpiDTO();
		int totalNumberOfRows = 0;
		int totalNumberOfCalls = 0;
		int totalNumberOfMessages = 0;
		Set<Integer> originCountryCodeSet = new HashSet<>();
		Set<Integer> destinationCountrycodeSet = new HashSet<>();
		Map<String, Long> jsonProcessDurationMap = new HashMap<>();

		List<ProcessedFile> processedFileList = processedFileService.findAll();

		// We go through all the JSON files and get the data of the metrics we want to
		// show summed up in the KPIS endpoint.
		for (ProcessedFile processedFile : processedFileList) {
			FileMetrics fileMetrics = processedFile.getFileMetrics();
			totalNumberOfRows += fileMetrics.getRowSize();
			totalNumberOfCalls += fileMetrics.getNumerOfCalls();
			totalNumberOfMessages += fileMetrics.getNumerOfMessages();

			fileMetrics.getCountryCodeData().stream()
					.filter(countryCodeData -> countryCodeData.getNumberOfCallsOrigin() > 0)
					.forEach(countryCodeData -> originCountryCodeSet.add(countryCodeData.getCountryCode()));

			fileMetrics.getCountryCodeData().stream()
					.filter(countryCodeData -> countryCodeData.getNumberOfCallsDestination() > 0)
					.forEach(countryCodeData -> destinationCountrycodeSet.add(countryCodeData.getCountryCode()));

			jsonProcessDurationMap.put(String.valueOf(processedFile.getFileDate()),
					fileMetrics.getProcessDurationMilliseconds());
		}

		// We map the object once all the JSON files have been traversed.
		kpiDTO.setTotalProcessedJsonFiles(processedFileList.size());
		kpiDTO.setTotalNumberOfRows(totalNumberOfRows);
		kpiDTO.setTotalNumerOfCalls(totalNumberOfCalls);
		kpiDTO.setTotalNumberOfMessages(totalNumberOfMessages);
		kpiDTO.setTotalDifferentDestinationCountryCodes(destinationCountrycodeSet.size());
		kpiDTO.setTotalNumberDifferentOriginCountryCodes(originCountryCodeSet.size());
		kpiDTO.setJsonProcessDuretionMillisMap(jsonProcessDurationMap);

		return kpiDTO;
	}
}
