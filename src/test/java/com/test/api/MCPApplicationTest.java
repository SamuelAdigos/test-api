package com.test.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonStreamParser;
import com.test.api.model.dto.CommunicationDTO;
import com.test.api.util.MCPUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class MCPApplicationTest {
 
    @Test
    void convertTwoRowJsonToDTO() {
        final String jsonString = "{" +
                "\"message_type\": \"CALL\"," +
                "\"timestamp\": 1517645700," +
                "\"origin\": 34969000001," +
                "\"destination\": 34969000101," +
                "\"duration\": 120," +
                "\"status_code\": \"OK\"," +
                "\"status_description\": \"OK\"" +
                "}\n" +
                "{" +
                "\"message_type\": \"MSG\"," +
                "\"timestamp\": 1517559300," +
                "\"origin\": 34960000002," +
                "\"destination\": 34960000102," +
                "\"message_content\": \"1. HELLO\"," +
                "\"message_status\": \"SEEN\"" +
                "}";

        List<CommunicationDTO> communicationDTOList = new ArrayList<>();

        Gson gson = new GsonBuilder().create();
        JsonStreamParser parser = new JsonStreamParser(jsonString);
        while (parser.hasNext()) {
            CommunicationDTO communicationDTO = gson.fromJson(parser.next(), CommunicationDTO.class);
            communicationDTOList.add(communicationDTO);
        }
        assertEquals(2, communicationDTOList.size());
    }

    @Test
    void convertCallJsonToDTO() {
        final String jsonString = "{" +
                "\"message_type\": \"CALL\"," +
                "\"timestamp\": 1517645700," +
                "\"origin\": 34969000001," +
                "\"destination\": 34969000101," +
                "\"duration\": 120," +
                "\"status_code\": \"OK\"," +
                "\"status_description\": \"OK\"" +
                "}";

        List<CommunicationDTO> communicationDTOList = new ArrayList<>();

        Gson gson = new GsonBuilder().create();
        JsonStreamParser parser = new JsonStreamParser(jsonString);
        while (parser.hasNext()) {
            CommunicationDTO communicationDTO = gson.fromJson(parser.next(), CommunicationDTO.class);
            communicationDTOList.add(communicationDTO);
        }
        assertEquals(1, communicationDTOList.size());
        CommunicationDTO communicationDTO = communicationDTOList.get(0);
        assertEquals("CALL", communicationDTO.getMessageType());
        assertEquals("1517645700", communicationDTO.getTimestamp());
        assertEquals("34969000001", communicationDTO.getOrigin());
        assertEquals("34969000101", communicationDTO.getDestination());
        assertEquals("120", communicationDTO.getDuration());
        assertEquals("OK", communicationDTO.getStatusCode());
        assertEquals("OK", communicationDTO.getStatusDescription());
    }

    @Test
    void convertMessageJsonToDTO() {
        final String jsonString = "{" +
                "\"message_type\": \"MSG\"," +
                "\"timestamp\": 1517559300," +
                "\"origin\": 34960000002," +
                "\"destination\": 34960000102," +
                "\"message_content\": \"1. HELLO\"," +
                "\"message_status\": \"SEEN\"" +
                "}";

        List<CommunicationDTO> communicationDTOList = new ArrayList<>();

        Gson gson = new GsonBuilder().create();
        JsonStreamParser parser = new JsonStreamParser(jsonString);
        while (parser.hasNext()) {
            CommunicationDTO communicationDTO = gson.fromJson(parser.next(), CommunicationDTO.class);
            communicationDTOList.add(communicationDTO);
        }
        assertEquals(1, communicationDTOList.size());
        CommunicationDTO communicationDTO = communicationDTOList.get(0);
        assertEquals("MSG", communicationDTO.getMessageType());
        assertEquals("1517559300", communicationDTO.getTimestamp());
        assertEquals("34960000002", communicationDTO.getOrigin());
        assertEquals("34960000102", communicationDTO.getDestination());
        assertEquals("1. HELLO", communicationDTO.getMessageContent());
        assertEquals("SEEN", communicationDTO.getMessageStatus());
    }

    @Test
    void parseCountryCode() {
        String phoneNumber = "34960000002";
        String countryCode = MCPUtils.getCountryCode(phoneNumber);
        assertEquals("34", countryCode);
    }

}
