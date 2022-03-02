package com.test.api.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonStreamParser;
import com.test.api.model.data.*;
import com.test.api.model.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);
    private static final String CALL = "CALL";
    private static final String MSG = "MSG";
    private static final String CALL_OK = "OK";
    private static final String CALL_KO = "KO";
    private static final String MSG_SEEN = "SEEN";
    private static final String MSG_DELIVERED = "DELIVERED";
    @Value("${mcp.json-url}")
    private String apiPath;
    @Value("${word-list}")
    private List<String> wordList;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ProcessedFileService processedFileService;

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
            } catch (Exception ex){
                LOGGER.error("Unexpected error parsing json", ex);
                rowErrors++;
            }

            ProcessedFile processedFile = new ProcessedFile();
            processedFile.setFileDate(date);

            processedFile.setFileMetrics(
                    getFileMetrics(communicationList, start, processedFile, rowErrors)
            );
            processedFileService.save(processedFile);

            processResponseDTO.setMessage("PROCESSED");
        } else {
            processResponseDTO.setMessage("NO FILE TO PROCESS");
        }

        return processResponseDTO;
    }

    private Set<WordOccurrence> getWordOccurrencesDataset(List<CommunicationDTO> communicationList, FileMetrics fileMetrics) {

        Map<String, Integer> wordCountMap = new HashMap<>();
        wordList.forEach(word -> wordCountMap.put(word, 0));

        List<String> messageContentList = communicationList.stream()
                .filter(communicationDTO -> communicationDTO.getMessageType().equals(MSG))
                .map(CommunicationDTO::getMessageContent).collect(Collectors.toList());

        messageContentList.forEach(messageContent -> {
            for (String word : wordList) {
                if (messageContent.contains(word)) {
                    Integer occurrences = wordCountMap.get(word);
                    occurrences++;
                    wordCountMap.put(word, occurrences);
                }
            }
        });

        return wordCountMap.entrySet().stream().map(e -> {
            WordOccurrence wordOccurrence = new WordOccurrence();
            wordOccurrence.setWord(e.getKey());
            wordOccurrence.setCount(e.getValue());
            wordOccurrence.setFileMetrics(fileMetrics);
            return wordOccurrence;
        }).collect(Collectors.toSet());
    }

    private RowErrors getRowErrors(List<CommunicationDTO> communicationList, FileMetrics fileMetrics, int jsonRowErrors) {
        int fieldErrors = 0;
        int blankContent = 0;
        int missingFields = 0;
        double successCallsRatio;
        int totalCalls = 0;
        int totalMessages = 0;
        int okCalls = 0;

        for (CommunicationDTO communicationDTO : communicationList) {
            String messageType = communicationDTO.getMessageType();
            String timestampString = communicationDTO.getTimestamp();
            String originString = communicationDTO.getOrigin();
            String destinationString = communicationDTO.getDestination();
            String durationString = communicationDTO.getDuration();

            if (messageType.equals(CALL) || messageType.equals(MSG)) {
                boolean foundFieldError = false;
                boolean foundMissingField;

                foundMissingField = checkMissingFields(timestampString, originString, destinationString);

                switch (messageType) {
                    case CALL:
                        String statusCode = communicationDTO.getStatusCode();
                        String statusDescriptionString = communicationDTO.getStatusDescription();

                        if (!foundMissingField) {
                            if (statusCode == null || statusCode.isEmpty()
                                    || statusDescriptionString == null || statusDescriptionString.isEmpty()) {
                                foundMissingField = true;
                            }
                        }

                        if (statusCode != null) {
                            if (!statusCode.equals(CALL_OK) && !statusCode.equals(CALL_KO)) {
                                foundFieldError = true;
                            }

                            if (statusCode.equals(CALL_OK)) {
                                okCalls++;
                            }
                        }

                        totalCalls++;

                        break;
                    case MSG:
                        String messageContent = communicationDTO.getMessageContent();
                        String messageStatus = communicationDTO.getMessageStatus();

                        if (!foundMissingField) {
                            if (messageContent == null || messageContent.isEmpty()
                                    || messageStatus == null || messageStatus.isEmpty()) {
                                foundMissingField = true;
                            }
                        }

                        if (messageStatus != null) {
                            if (!messageStatus.equals(MSG_SEEN) && !messageStatus.equals(MSG_DELIVERED)) {
                                foundFieldError = true;
                            }
                        }

                        if (messageContent != null && messageContent.isEmpty()) {
                            blankContent++;
                        }

                        totalMessages++;
                        break;
                }


                try {
                    Long.parseLong(timestampString);
                    Long.parseLong(originString);
                    Long.parseLong(destinationString);
                    Long.parseLong(durationString);
                } catch (NumberFormatException e) {
                    foundFieldError = true;
                }

                if (foundFieldError) {
                    fieldErrors++;
                }

                if (foundMissingField) {
                    missingFields++;
                }
            } else {
                fieldErrors++;
                missingFields++;
            }
        }

        if(jsonRowErrors > 0){
            fieldErrors+=jsonRowErrors;
            missingFields+=jsonRowErrors;
        }

        successCallsRatio = findSuccessCallRatio(okCalls, totalCalls);
        fileMetrics.setSuccessCallsPercentage(successCallsRatio);
        fileMetrics.setNumerOfCalls(totalCalls);
        fileMetrics.setNumerOfMessages(totalMessages);

        RowErrors rowErrors = new RowErrors();
        rowErrors.setFieldErrors(fieldErrors);
        rowErrors.setMissingFields(missingFields);
        rowErrors.setMissingFields(missingFields);
        rowErrors.setBlankContent(blankContent);
        rowErrors.setFileMetrics(fileMetrics);

        return rowErrors;
    }

    private boolean checkMissingFields(String timestampString, String originString, String destinationString) {
        return timestampString == null || timestampString.isEmpty()
                || originString == null || originString.isEmpty()
                || destinationString == null || destinationString.isEmpty();
    }

    private double findSuccessCallRatio(int okCalls, int totalCalls) {
        final DecimalFormat df = new DecimalFormat("0,00");
        double ratio = (double) okCalls / totalCalls;
        String formattedRatio = df.format(ratio * 100);
        return Double.parseDouble(formattedRatio);
    }

    private double getAverageDuration(int totalCalls, int totalDuration) {
        final DecimalFormat df = new DecimalFormat("0,00");
        double average = (double) totalDuration / totalCalls;
        String formattedAverage = df.format(average);
        return Double.parseDouble(formattedAverage);
    }

    private FileMetrics getFileMetrics(List<CommunicationDTO> communicationList, Instant start, ProcessedFile processedFile, int jsonRowErrors) {
        FileMetrics fileMetrics = new FileMetrics();
        Set<Integer> originCountryCodeSet = new HashSet<>();
        Set<Integer> destinationCountryCodeSet = new HashSet<>();
        Set<Integer> completeCountryCodeSet = new HashSet<>();

        for (CommunicationDTO communicationDTO : communicationList) {
            if (communicationDTO.getMessageType() != null && communicationDTO.getMessageType().equals(CALL)) {
                if (communicationDTO.getOrigin() != null && !communicationDTO.getOrigin().isEmpty()) {
                    try {
                        int originCc = Integer.parseInt(communicationDTO.getOrigin().substring(0, 2));
                        originCountryCodeSet.add(originCc);
                    } catch (NumberFormatException e) {
                        LOGGER.error("Unexpected error parsing origin and destination codes", e);
                    }
                }

                if (communicationDTO.getDestination() != null && !communicationDTO.getDestination().isEmpty()) {
                    try {
                        int destinationCc = Integer.parseInt(communicationDTO.getDestination().substring(0, 2));
                        destinationCountryCodeSet.add(destinationCc);
                    } catch (NumberFormatException e) {
                        LOGGER.error("Unexpected error parsing origin and destination codes", e);
                    }
                }
            }

        }

        completeCountryCodeSet.addAll(originCountryCodeSet);
        completeCountryCodeSet.addAll(destinationCountryCodeSet);

        Map<Integer, CountryCodeData> countryCodeDataMap = completeCountryCodeSet.stream().map(cc -> {
            CountryCodeData countryCodeData = new CountryCodeData();
            countryCodeData.setCountryCode(cc);
            countryCodeData.setFileMetrics(fileMetrics);
            return countryCodeData;
        }).collect(Collectors.toMap(CountryCodeData::getCountryCode, Function.identity()));


        communicationList.forEach(communicationDTO -> {

            if (communicationDTO.getMessageType() != null && communicationDTO.getMessageType().equals(CALL)) {
                if (communicationDTO.getOrigin() != null && communicationDTO.getOrigin().length() >= 2) {
                    try {
                        int originCc = Integer.parseInt(communicationDTO.getOrigin().substring(0, 2));
                        CountryCodeData countryCodeData = countryCodeDataMap.get(originCc);
                        int originCalls = countryCodeData.getNumberOfCallsOrigin();
                        originCalls++;
                        countryCodeData.setNumberOfCallsOrigin(originCalls);
                    } catch (NumberFormatException e) {
                        LOGGER.error("Unexpected error parsing origin and destination codes", e);
                    }
                }

                if (communicationDTO.getDestination() != null && communicationDTO.getDestination().length() >= 2) {
                    try {
                        int destinationCc = Integer.parseInt(communicationDTO.getDestination().substring(0, 2));
                        CountryCodeData countryCodeData = countryCodeDataMap.get(destinationCc);
                        int destinationCalls = countryCodeData.getNumberOfCallsDestination();
                        destinationCalls++;
                        countryCodeData.setNumberOfCallsDestination(destinationCalls);
                    } catch (NumberFormatException e) {
                        LOGGER.error("Unexpected error parsing origin and destination codes", e);
                    }
                }
            }
        });

        for (CommunicationDTO communicationDTO : communicationList) {
            for (int cc : completeCountryCodeSet) {
                String origin = communicationDTO.getOrigin();
                String destination = communicationDTO.getDestination();
                String durationString = communicationDTO.getDuration();
                int originCc = -1;
                int destinationCc = -1;

                if (origin != null && origin.length() >= 2) {
                    try {
                        originCc = Integer.parseInt(origin.substring(0, 2));
                    } catch (NumberFormatException e) {
                        LOGGER.error("Unexpected error parsing origin and destination codes", e);
                    }
                }
                if (destination != null && destination.length() >= 2) {
                    try {
                        destinationCc = Integer.parseInt(destination.substring(0, 2));
                    } catch (NumberFormatException e) {
                        LOGGER.error("Unexpected error parsing origin and destination codes", e);
                    }
                }

                if (originCc == cc || destinationCc == cc) {
                    if (durationString != null) {
                        try {
                            int duration = Integer.parseInt(durationString);
                            CountryCodeData countryCodeData = countryCodeDataMap.get(originCc);
                            Integer ccTotalDuration = countryCodeData.getTotalDuration();
                            ccTotalDuration += duration;
                            countryCodeData.setTotalDuration(ccTotalDuration);
                        } catch (NumberFormatException e) {
                            LOGGER.error("Unexpected error parsing origin and destination codes", e);
                        }
                    }
                }
            }
        }

        countryCodeDataMap.values().forEach(countryCodeData -> {
            int totalCalls = countryCodeData.getNumberOfCallsDestination() + countryCodeData.getNumberOfCallsOrigin();
            countryCodeData.setAverageCallDuration(
                    getAverageDuration(
                            totalCalls,
                            countryCodeData.getTotalDuration()
                    )
            );
        });

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();

        fileMetrics.setProcessedFile(processedFile);
        fileMetrics.setOriginCountryCodes(originCountryCodeSet.size());
        fileMetrics.setDestinationCountryCodes(destinationCountryCodeSet.size());
        fileMetrics.setProcessDurationMilliseconds(timeElapsed);
        fileMetrics.setRowSize(communicationList.size() + jsonRowErrors);
        fileMetrics.setDifferentOriginCountryCodes(originCountryCodeSet.size());
        fileMetrics.setDifferentDestinationCountryCodes(destinationCountryCodeSet.size());
        fileMetrics.setRowErrors(
                getRowErrors(communicationList, fileMetrics, jsonRowErrors)
        );
        fileMetrics.setWordOccurrences(
                getWordOccurrencesDataset(communicationList, fileMetrics)
        );
        Set<CountryCodeData> countryCodeData = new HashSet<>(countryCodeDataMap.values());
        fileMetrics.setCountryCodeData(
                countryCodeData
        );

        return fileMetrics;
    }


    public List<ProcessedFile> getFiles() {
        return processedFileService.findAll();
    }

    public MetricsDTO getMetrics(Integer date) {
        MetricsDTO metricsDTO = new MetricsDTO();

        ProcessedFile processedFile = processedFileService.findByDate(date);

        if (processedFile != null) {
            Set<CountryCodeDataDTO> countryCodeDataDTOSet = processedFile.getFileMetrics()
                    .getCountryCodeData().stream()
                    .map(CountryCodeDataDTO::toCountryCodeDataDTO)
                    .collect(Collectors.toSet());

            metricsDTO.setCountryCodeData(countryCodeDataDTOSet);

            Set<WordOccurrenceDTO> wordOccurrenceDTOSet = processedFile.getFileMetrics()
                    .getWordOccurrences().stream()
                    .map(WordOccurrenceDTO::toWordOccuranceDTO)
                    .collect(Collectors.toSet());

            metricsDTO.setWordOccurrences(wordOccurrenceDTOSet);

            RowErrors rowErrors = processedFile.getFileMetrics().getRowErrors();
            metricsDTO.setRowsWithFieldErrors(rowErrors.getFieldErrors());
            metricsDTO.setMessageWithBlankContent(rowErrors.getBlankContent());
            metricsDTO.setRowsWithMissingFields(rowErrors.getMissingFields());
            metricsDTO.setSuccededFailedCallsRatio(processedFile.getFileMetrics().getSuccessCallsPercentage());
        }

        return metricsDTO;
    }

    public KpiDTO getKpis() {
        KpiDTO kpiDTO = new KpiDTO();
        List<ProcessedFile> processedFileList = processedFileService.findAll();

        int totalNumberOfRows = 0;
        int totalNumberOfCalls = 0;
        int totalNumberOfMessages = 0;
        int totalNumberOfDifferentOriginCountryCodes = 0;
        int totalNumberOfDifferentDestinationCountryCodes = 0;
        Map<String, Long> jsonProcessDurationMap = new HashMap<>();

        for (ProcessedFile processedFile : processedFileList) {
            FileMetrics fileMetrics = processedFile.getFileMetrics();
            totalNumberOfRows += fileMetrics.getRowSize();
            totalNumberOfCalls += fileMetrics.getNumerOfCalls();
            totalNumberOfMessages += fileMetrics.getNumerOfMessages();
            totalNumberOfDifferentOriginCountryCodes += fileMetrics.getDifferentOriginCountryCodes();
            totalNumberOfDifferentDestinationCountryCodes += fileMetrics.getDifferentDestinationCountryCodes();
            jsonProcessDurationMap.put(String.valueOf(processedFile.getFileDate()), fileMetrics.getProcessDurationMilliseconds());
        }

        kpiDTO.setTotalProcessedJsonFiles(processedFileList.size());
        kpiDTO.setTotalNumberOfRows(totalNumberOfRows);
        kpiDTO.setTotalNumerOfCalls(totalNumberOfCalls);
        kpiDTO.setTotalNumberOfMessages(totalNumberOfMessages);
        kpiDTO.setTotalDifferentDestinationCountryCodes(totalNumberOfDifferentDestinationCountryCodes);
        kpiDTO.setTotalNumberDifferentOriginCountryCodes(totalNumberOfDifferentOriginCountryCodes);
        kpiDTO.setJsonProcessDuretionMillisMap(jsonProcessDurationMap);

        return kpiDTO;
    }
}
