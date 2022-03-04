# TEST-API (MCP)
    Microservice with API created with Spring Boot as a base that retrieves information related to a mobile communication platform and processes it generating metrics for each JSON file obtained and general KPI statistics. This data will be stored in a relational database (in this case I used MariaDB).
> The following **principal frameworks** were **used**: [Spring Boot](https://spring.io/projects/spring-boot "Spring Boot"), [Lombok](https://projectlombok.org/ "Lombok"), [JUnit](https://junit.org/junit5/ "JUnit") and [GSON](https://github.com/google/gson "GSON").


------------
## **HTTP ENDPOINTS**:
### -  **GET /file/{date}**

Processes a specific JSON file through a value corresponding to the date in YYYYYYMMDD format.

Once the JSON file is processed it returns a "PROCESSED" message.

If any error occurs not contemplated by the application it will return a 500 error. Perform all the metrics described in the task and explained below in the query endpoint of these metrics (GET /file/{date}/metrics).

### - **GET /file/all**

Displays all JSON files processed by the API and stored in the database.

### - **GET /file/{date}/metrics**

Displays the metrics of a given JSON file processed by the API through a value corresponding to the date in YYYYYYMMDD format.

#### **EXPLANATION OF METRICS (under my point of view):**
    rowsWithMissingFields: it will count all the rows in the file where any of the mandatory fields contemplated in the task are missing depending on the type of row (CALL or MSG) and will not process any of the other metrics for that row as it is considered "wrong row".
    messageWithBlankContent: It will count all the "message_content" fields with blank content.
    rowsWithFieldErrors: Will count all other fields that have blank content or do not follow a correct format specific to each field (message_type can only be CALL or MSG, timestamp must have a correct format, source and destination numbers must follow a correct number format, duration must follow its time format well, status_code must be OK or KO only, message_status content must be "DELIVERED" or "SEEN" only).
    countryCodeData: Stores the number of origin and destination calls and an average in minutes of the calls grouped by the country code of each of the countries in the file.
    succededFailedCallsRatio: Averages the number of OK calls over the total number of successful calls in the file.
    wordOcurrences: For each word specified in the word-list of the application.yml file (which can be modified in the future), a counter of occurrences in the correct messages of the JSON file will be performed.


### **GET /file/kpis**
Shows the KPIS statistics of all the files processed by the API and stored in the DB. 

These statistics are:
    totalProcessedJsonFiles: Number of JSON files processed by the API.
    totalNumberOfRows: Number of rows of the processed JSON files.
    totalNumberOfCalls: Number of rows with "CALL" as "message_type".
    totalNumberOfMessagess: Number of rows with "MSG" as "message_type".
    totalNumberDifferentOriginCountryCodes: Number of distinct country codes in the "origin" fields found in the processed JSON files.
    totalDifferentDestinationCountryCodes: Number of distinct country codes in the "destination" fields found in the JSON files processed.
    jsonProcessDurationMillisMap: Grouping by the date of each JSON file, the execution time of the JSON file is stored in milliseconds.


------------
## **UNIT TESTS:**
Unit tests are also added for some of the functions programmed in the code (MCPAplicationTest):
- Testing of mapping of two JSON rows to DTO.
- Testing of mapping of specific fields of a messsage_type "CALL" to DTO.
- Testing of mapping of specific fields of a messsage_type "MSG" to DTO.
- Testing of obtaining the country code of a specific telephone number.


------------

#### IMPORTANT
**You must modify the file src/main/resources/application.yml to place the data of the relational database that you want to use to launch the application. The tables and columns will be created automatically at application launch and will be updated in case the metrics of an already stored JSON file change once it is reprocessed.**

------------
> *VAS-TEST Requirements*: https://github.com/vas-test/test1
