/**
 * MonitoringServiceTest.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.monitoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Calendar;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import it.unipd.dei.softplat.http.service.HttpClientService;
import it.unipd.dei.softplat.monitoring.controller.MonitoringController;
import it.unipd.dei.softplat.monitoring.model.MonitoringRequest;

/**
 * This class is intended to test the MonitoringService.
 * It contains test methods to validate the functionality of the MonitoringController and the MonitoringRequest model.
 */
@SpringBootTest
public class MonitoringTest {

    @MockBean
    private HttpClientService httpClientService;
    
    @Autowired @InjectMocks
    private MonitoringController controller_test;

    /**
     * This test method is intended to test the startMonitoring method of the MonitoringController.
     * It creates a valid and an invalid MonitoringRequest and calls the startMonitoring method.
     */
    @Test
    public void testStartMonitoring() {
        // Mock configuration
        when(httpClientService.postRequest(
                eq("http://localhost:8080/datamanager/save-articles/"),
                anyString()
            )
        ).thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));
        when(httpClientService.postRequest(
            eq("http://localhost:8080/client/status/"),
            anyString()
            )
        ).thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));

        // Example of a valid request
        MonitoringRequest request = new MonitoringRequest();
        request.setIssueQuery("example issue query");
        request.setLabel("example label");
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
        Date startDate = cal.getTime();
        request.setStartDate(startDate);
        cal.set(2023, Calendar.DECEMBER, 31, 23, 59, 59);
        Date endDate = cal.getTime();
        request.setEndDate(endDate);

        // Call the startMonitoring method and check if the ResponseEntity is successful.
        ResponseEntity <?> response = controller_test.startMonitoring(request);
        
        // Assert that the response is not null and has a status code of 200 OK
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Response should have status code 200 OK");

        // Verify that the postRequest method of MonitoringService was called with the correct parameters
        verify(httpClientService).postRequest(eq("http://localhost:8080/datamanager/save-articles/"), anyString());
        verify(httpClientService).postRequest(eq("http://localhost:8080/client/status/"), anyString());

        // Example of an invalid request
        MonitoringRequest invalidRequest = new MonitoringRequest();
        invalidRequest.setIssueQuery("");
        invalidRequest.setLabel("example label");
        cal = Calendar.getInstance();
        cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
        startDate = cal.getTime();
        request.setStartDate(startDate);
        cal.set(2023, Calendar.DECEMBER, 31, 23, 59, 59);
        endDate = cal.getTime();
        request.setEndDate(endDate);

        // Call the startMonitoring method with an invalid request and check if it returns a bad request response.
        ResponseEntity <?> invalidResponse = controller_test.startMonitoring(invalidRequest);

        // Assert that the response is not null and has a status code of 400 Bad Request
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, invalidResponse.getStatusCode(), "Response should have status code 400 Bad Request");
    }

    /**
     * This test method is intended to test the validation of the MonitoringRequest object.
     * It checks all the getters and setters of the MonitoringRequest class.
     */
    @Test
    public void testRequestValidationGetterSetter() {
        MonitoringRequest request = new MonitoringRequest();

        // Set the issue query
        request.setIssueQuery("test issue query");
        assertEquals("test issue query", request.getIssueQuery(), "Issue query should match the set value");
        // Set the label
        request.setLabel("test label");
        assertEquals("test label", request.getLabel(), "Label should match the set value");
        // Set the start date
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
        Date startDate = cal.getTime();
        request.setStartDate(startDate);
        assertEquals(startDate, request.getStartDate(), "Start date should match the set value");
        // Set the end date
        cal.set(2023, Calendar.DECEMBER, 31, 23, 59, 59);
        Date endDate = cal.getTime();
        request.setEndDate(endDate);
        assertEquals(endDate, request.getEndDate(), "End date should match the set value");
    }

    /**
     * This test method is intended to test the validation of the MonitoringRequest object for null values.
     * It checks if the issue query, label, start date, and end date are not null.
     * If any of these values are null, it should throw an IllegalArgumentException.
     */
    @Test
    public void testRequestValidationNullValues() {
        MonitoringRequest request = new MonitoringRequest();
        // Check if the request rejects null values, empty strings, or invalid formats
        // setIssueQuery with null value
        try {
            request.setIssueQuery(null);
        } 
        catch (IllegalArgumentException e) {
            assertEquals("Issue query cannot be null or empty.", e.getMessage(), "Should throw an exception for null issue query");
        }
        // setLabel with null value
        try {
            request.setLabel(null);
        } 
        catch (IllegalArgumentException e) {
            assertEquals("Label cannot be null or empty.", e.getMessage(), "Should throw an exception for null label");
        }
        // setStartDate with null value
        try {
            request.setStartDate(null);
        } 
        catch (IllegalArgumentException e) {
            assertEquals("Start date cannot be null or empty.", e.getMessage(), "Should throw an exception for null start date");
        }
    }

    /**
     * This test method is intended to test the validation of the MonitoringRequest object for empty values.
     * It checks if the issue query, label, start date, and end date are not empty strings.
     * If any of these values are empty, it should throw an IllegalArgumentException.
     */
    @Test
    public void testRequestValidationEmptyValues() {
        MonitoringRequest request = new MonitoringRequest();
        // setIssueQuery with empty string
        try {
            request.setIssueQuery("");
        } 
        catch (IllegalArgumentException e) {
            assertEquals("Issue query cannot be null or empty.", e.getMessage(), "Should throw an exception for empty issue query");
        }
        // setLabel with empty string
        try {
            request.setLabel("");
        } 
        catch (IllegalArgumentException e) {
            assertEquals("Label cannot be null or empty.", e.getMessage(), "Should throw an exception for empty label");
        }
    }

    /**
     * This test method is intended to test the validation of the MonitoringRequest object for date order.
     * It checks if the start date is before the end date and vice versa.
     * If the dates are in the wrong order, it should throw an IllegalArgumentException.
     */
    @Test
    public void testRequestValidationDateOrder() {
        MonitoringRequest request = new MonitoringRequest();
        Calendar cal_01_01_2023 = Calendar.getInstance();
        cal_01_01_2023.set(2023, Calendar.JANUARY, 2, 0, 0, 0);
        Date endDate = cal_01_01_2023.getTime();
        Calendar cal_02_01_2023 = Calendar.getInstance();
        cal_02_01_2023.set(2023, Calendar.JANUARY, 1, 23, 59, 59);
        Date startDate = cal_02_01_2023.getTime();
        try{
            request.setStartDate(startDate);
            request.setEndDate(endDate); // End date before start date
        }
        catch (IllegalArgumentException e) {
            assertEquals("End date cannot be before start date.", e.getMessage(), "Should throw an exception for end date before start date");
        }
        request = new MonitoringRequest();
        endDate = cal_01_01_2023.getTime();
        startDate = cal_02_01_2023.getTime(); 
        try {
            request.setEndDate(endDate);
            request.setStartDate(startDate); // Start date after end date
        }
        catch (IllegalArgumentException e) {
            assertEquals("Start date cannot be after end date.", e.getMessage(), "Should throw an exception for start date after end date");
        }
    }
}
