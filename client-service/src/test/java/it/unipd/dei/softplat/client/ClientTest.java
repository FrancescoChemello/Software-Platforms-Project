/**
 * ClientTest.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import it.unipd.dei.softplat.client.controller.ClientController;
import it.unipd.dei.softplat.client.service.ClientService;
import it.unipd.dei.softplat.client.dto.MessageDTO;
import it.unipd.dei.softplat.client.model.QueryResult;
import it.unipd.dei.softplat.client.model.QueryTopic;
import it.unipd.dei.softplat.http.service.HttpClientService;

/**
 * This class is intended to test the ClientService and ClientController.
 * It contains test methods to validate the functionality of the ClientController and the ClientService.
 */
@SpringBootTest
public class ClientTest {
    
    @MockBean
    private HttpClientService httpClientService;

    @Autowired @InjectMocks
    private ClientController controller_test;

    @Autowired @InjectMocks
    private ClientService service_test;

    /**
     * This test method is intended to test the getQueryResult method of the ClientController.
     * It creates a valid and an invalid QueryResult and calls the getQueryResult method.
     */
    @Test
    public void testGetQueryResult() {
        // Create a valid QueryTopic object
        QueryTopic queryTopic_1 = new QueryTopic(
            "article_id_1",
            List.of("topic_word_1", "topic_word_2")
        );
        QueryTopic queryTopic_2 = new QueryTopic(
            "article_id_2",
            List.of("topic_word_3", "topic_word_4")
        );
        // Create a valid QueryResult object
        QueryResult queryResult = new QueryResult(
            "query_test",
            List.of(queryTopic_1, queryTopic_2)
        );

        // Call the getQueryResult method with the valid QueryResult object
        ResponseEntity<?> response = controller_test.getQueryResult(queryResult);
        // Assert that the response is not null
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Response should have status code 200 OK");

        // Invalid QueryResult object
        QueryResult invalidQueryResult = new QueryResult(
            null, // Invalid query
            List.of(queryTopic_1, queryTopic_2)
            );
            
        // Call the getQueryResult method with an invalid QueryResult object
        ResponseEntity<?> invalidResponse = controller_test.getQueryResult(invalidQueryResult);
        // Assert that the response is not null
        assertNotNull(invalidResponse, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, invalidResponse.getStatusCode(), "Response should have status code 400 BAD REQUEST");

        // Invalid QueryResult object with null topics
        QueryResult invalidQueryResultWithNullTopics = new QueryResult(
            "query_test",
            null // Invalid topics
        );

        // Call the getQueryResult method with an invalid QueryResult object
        ResponseEntity<?> invalidResponseWithNullTopics = controller_test.getQueryResult(invalidQueryResultWithNullTopics);
        // Assert that the response is not null
        assertNotNull(invalidResponseWithNullTopics, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, invalidResponseWithNullTopics.getStatusCode(), "Response should have status code 400 BAD REQUEST");
    }

    /**
     * This test method is intended to test the getStatus method of the ClientController.
     * It creates a valid and an invalid MessageDTO and calls the getStatus method.
     */
    @Test
    public void testGetStatus() {
        // Create a valid MessageDTO object
        MessageDTO messageDTO = new MessageDTO("MONITORING", "Monitoring test status OK!");

        // Call the getStatus method with the valid MessageDTO object
        ResponseEntity<?> response = controller_test.getStatus(messageDTO);
        // Assert that the response is not null
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Response should have status code 200 OK");

        // Invalid MessageDTO object
        MessageDTO invalidMessageDTO = new MessageDTO(null, null);
        
        // Call the getStatus method with an invalid MessageDTO object
        ResponseEntity<?> invalidResponse = controller_test.getStatus(invalidMessageDTO);
        // Assert that the response is not null
        assertNotNull(invalidResponse, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, invalidResponse.getStatusCode(), "Response should have status code 400 BAD REQUEST");

        // Invalid MessageDTO object with empty status
        MessageDTO invalidMessageDTOWithEmptyStatus = new MessageDTO("", "Monitoring test status OK!");

        // Call the getStatus method with an invalid MessageDTO object
        ResponseEntity<?> invalidResponseWithEmptyStatus = controller_test.getStatus(invalidMessageDTOWithEmptyStatus);
        // Assert that the response is not null
        assertNotNull(invalidResponseWithEmptyStatus, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, invalidResponseWithEmptyStatus.getStatusCode(), "Response should have status code 400 BAD REQUEST");
    }

    /**
     * This test method is intended to test the sendMonitoringRequest method of the ClientService.
     * It mocks the HttpClientService and verifies that the postRequest method is called with the correct parameters.
     */
    @Test
    public void testSendMonitoringRequest() {
        // Mock configuration
        when(httpClientService.postRequest(
            eq("http://monitoring-service:8081/monitoring/start/"),
            anyString())
        ).thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));

        // Create a valid issue string
        String issueString = "artificial intelligence";
        String label = "ai";
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
        Date startDate = cal1.getTime();
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2023, Calendar.DECEMBER, 31, 23, 59, 59);
        Date endDate = cal2.getTime();

        // Call the sendMonitoringRequest method
        service_test.sendMonitoringRequest(issueString, label, startDate, endDate);
        // Verify that the postRequest method of HttpClientService was called with the correct parameters
        verify(httpClientService).postRequest(eq("http://monitoring-service:8081/monitoring/start/"), anyString());
    }

    /**
     * This test method is intended to test the sendQueryRequest method of the ClientService without a 
     * monitoring issueString active.
     * It mocks the HttpClientService and verifies that the postRequest method is never called.
     */
    @Test
    public void testSendQueryRequestWithNoMonitoring() {
        // Mock configuration
        when(httpClientService.postRequest(
            eq("http://mallet-service:8084/mallet/search/"),
            anyString())
        ).thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));

        // new ClientService instance without monitoring enabled
        service_test = new ClientService(httpClientService);

        // Create a valid query request
        String query = "test query";
        String corpus = "test corpus";

        Integer numTopics = 5;
        Integer numTopWordsPerTopic = 10;
        Date startDate = new Date();
        Date endDate = new Date();

        // Call the sendQueryRequest method
        service_test.sendQueryRequest(query, corpus, numTopics, numTopWordsPerTopic, startDate, endDate);
        
        // Verify that the postRequest method of HttpClientService was called with the correct parameters
        verify(httpClientService, times(0)).postRequest(eq("http://mallet-service:8084/mallet/search/"), anyString());
    }

    /**
     * This test method is intended to test the QueryTopic getter and setter methods.
     * It creates a QueryTopic object, sets its properties, and verifies that the getters return
     * the expected values.
     */
    @Test
    public void testQueryTopicGetterSetter() {
        QueryTopic queryTopic = new QueryTopic();
        String id = "test_id";
        List<String> topWords = List.of("word1", "word2");
        queryTopic.setId(id);
        queryTopic.setTopWords(topWords);

        // Assert that the getters return the expected values
        assertEquals("test_id", queryTopic.getId(), "QueryTopic ID should match");
        assertEquals(List.of("word1", "word2"), queryTopic.getTopWords(), "QueryTopic top words should match");
    }

    /**
     * This test method is intended to test the QueryTopic setter methods with null values.
     * It verifies that the setters throw IllegalArgumentException when null values are passed.
     */
    @Test
    public void testQueryTopicWithNullValues() {
        QueryTopic queryTopic = new QueryTopic();

        // Assert that the setters throw IllegalArgumentException when null values are passed
        assertThrows(IllegalArgumentException.class, () -> queryTopic.setId(null), "Expected exception for null ID");
        assertThrows(IllegalArgumentException.class, () -> queryTopic.setTopWords(null), "Expected exception for null top words");
    }

    /**
     * This test method is intended to test the QueryResult getter and setter methods.
     * It creates a QueryResult object, sets its properties, and verifies that the getters return
     * the expected values.
     */
    @Test
    public void testQueryResultGetterSetter() {
        QueryResult queryResult = new QueryResult();
        String query = "test_query";
        List<String> topWords = List.of("word1", "word2");
        QueryTopic queryTopic = new QueryTopic("article1", topWords);
        List<QueryTopic> topics = List.of(queryTopic);
        queryResult.setQuery(query);
        queryResult.setTopics(topics);

        // Assert that the getters return the expected values
        assertEquals("test_query", queryResult.getQuery(), "QueryResult query should match");
        assertEquals(List.of(queryTopic), queryResult.getTopics(), "QueryResult topics should match");
    }

    /**
     * This test method is intended to test the QueryResult setter methods with null values.
     * It verifies that the setters throw IllegalArgumentException when null values are passed.
     */
    @Test
    public void testQueryResultWithNullValues() {
        QueryResult queryResult = new QueryResult();
        
        // Assert that the setters throw IllegalArgumentException when null values are passed
        assertThrows(IllegalArgumentException.class, () -> queryResult.setQuery(null), "Expected exception for null query");
        assertThrows(IllegalArgumentException.class, () -> queryResult.setTopics(null), "Expected exception for null topics");
    }

    /**
     * This test method is intended to test the QueryResult setter methods with empty values.
     * It verifies that the setters throw IllegalArgumentException when empty values are passed.
     */
    @Test
    public void testQueryResultWithEmptyQuery() {
        QueryResult queryResult = new QueryResult();
        
        // Assert that the setter throws IllegalArgumentException when an empty query is passed
        assertThrows(IllegalArgumentException.class, () -> queryResult.setQuery(""), "Expected exception for empty query");
    }

    /**
     * This test method is intended to test the MessageDTO getter and setter methods.
     * It creates a MessageDTO object, sets its properties, and verifies that the getters return
     * the expected values.
     */
    @Test
    public void testMessageDTOGetterSetter() {
        MessageDTO messageDTO = new MessageDTO();
        String message = "Test message";
        String status = "Test status";
        messageDTO.setMessage(message);
        messageDTO.setStatus(status);

        // Assert that the getters return the expected values
        assertEquals("Test message", messageDTO.getMessage(), "MessageDTO message should match");
        assertEquals("Test status", messageDTO.getStatus(), "MessageDTO status should match");
    }

    /**
     * This test method is intended to test the MessageDTO setter methods with null values.
     * It verifies that the setters throw IllegalArgumentException when null values are passed.
     */
    @Test
    public void testMessageDTOWithNullValues() {
        MessageDTO messageDTO = new MessageDTO();
        
        // Assert that the setters throw IllegalArgumentException when null values are passed
        assertThrows(IllegalArgumentException.class, () -> messageDTO.setMessage(null), "Expected exception for null message");
        assertThrows(IllegalArgumentException.class, () -> messageDTO.setStatus(null), "Expected exception for null status");
    }

    /**
     * This test method is intended to test the MessageDTO setter methods with empty values.
     * It verifies that the setters throw IllegalArgumentException when empty values are passed.
     */
    @Test
    public void testMessageDTOWithEmptyValues() {
        MessageDTO messageDTO = new MessageDTO();
        
        // Assert that the setters throw IllegalArgumentException when empty values are passed
        assertThrows(IllegalArgumentException.class, () -> messageDTO.setMessage(""), "Expected exception for empty message");
        assertThrows(IllegalArgumentException.class, () -> messageDTO.setStatus(""), "Expected exception for empty status");
    }
}