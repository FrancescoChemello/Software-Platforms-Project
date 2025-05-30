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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import it.unipd.dei.softplat.monitoring.controller.MonitoringController;
import it.unipd.dei.softplat.monitoring.model.MonitoringRequest;

/**
 * This class is intended to test the MonitoringService.
 * It contains test methods to validate the functionality of the MonitoringController and the MonitoringRequest model.
 */
@SpringBootTest
public class MonitoringServiceTest {
    
    @Autowired
    private MonitoringController controller_test;

    /**
     * This test method is intended to test the startMonitoring method of the MonitoringController.
     * It creates a valid and an invalid MonitoringRequest and calls the startMonitoring method.
     */
    @Test
    public void testStartMonitoring() {
        // Example of a valid request
        MonitoringRequest request = new MonitoringRequest(
            "example issue query",
            "example label",
            "01/01/2023",
            "31/12/2023"
        );

        // Call the startMonitoring method and check if the ResponseEntity is successful.
        ResponseEntity <?> response = controller_test.startMonitoring(request);
        
        // Assert that the response is not null and has a status code of 200 OK
        assertNotNull(response, "Response should not be null");
        assertEquals(org.springframework.http.HttpStatus.OK, response.getStatusCode(), "Response should have status code 200 OK");

        // Example of an invalid request
        MonitoringRequest invalidRequest = new MonitoringRequest(
            "", // Empty issue query
            "example label",
            "01/01/2023",
            "31/12/2023"
        );

        // Call the startMonitoring method with an invalid request and check if it returns a bad request response.
        ResponseEntity <?> invalidResponse = controller_test.startMonitoring(invalidRequest);

        // Assert that the response is not null and has a status code of 400 Bad Request
        assertNotNull(response, "Response should not be null");
        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, invalidResponse.getStatusCode(), "Response should have status code 400 Bad Request");
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
        request.setStartDate("01/01/2023");
        assertEquals("01/01/2023", request.getStartDate(), "Start date should match the set value");
        // Set the end date
        request.setEndDate("31/12/2023");
        assertEquals("31/12/2023", request.getEndDate(), "End date should match the set value");
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
        // setEndDate with null value
        try {
            request.setEndDate(null);
        } 
        catch (IllegalArgumentException e) {
            assertEquals("End date cannot be null or empty.", e.getMessage(), "Should throw an exception for null end date");
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
        // setStartDate with empty string
        try {
            request.setStartDate("");
        } 
        catch (IllegalArgumentException e) {
            assertEquals("Start date cannot be null or empty.", e.getMessage(), "Should throw an exception for empty start date");
        }
        // setEndDate with empty string
        try {
            request.setEndDate("");
        } 
        catch (IllegalArgumentException e) {
            assertEquals("End date cannot be null or empty.", e.getMessage(), "Should throw an exception for empty end date");
        }
    }

    /**
     * This test method is intended to test the validation of the MonitoringRequest object for patterns.
     * It checks if the start date and end date follow the expected pattern of dd/MM/yyyy.
     * If the dates do not match the expected pattern, it should throw an IllegalArgumentException.
     */
    @Test
    public void testRequestValidationPatterns() {
        MonitoringRequest request = new MonitoringRequest();
        // setStartDate with invalid pattern
        try {
            request.setStartDate("01-01-2023"); // Invalid format
        } 
        catch (IllegalArgumentException e) {
            assertEquals("Invalid date format. Please use dd/MM/yyyy.", e.getMessage(), "Should throw an exception for invalid start date format");
        }
        try {
            request.setEndDate("01-01-2023"); // Invalid format
        } 
        catch (IllegalArgumentException e) {
            assertEquals("Invalid date format. Please use dd/MM/yyyy.", e.getMessage(), "Should throw an exception for invalid start date format");
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
        try{
            request.setStartDate("02/01/2023");
            request.setEndDate("01/01/2023"); // End date before start date
        }
        catch (IllegalArgumentException e) {
            assertEquals("End date cannot be before start date.", e.getMessage(), "Should throw an exception for end date before start date");
        }
        request = new MonitoringRequest();
        try {
            request.setEndDate("01/01/2023");
            request.setStartDate("02/01/2023"); // Start date after end date
        }
        catch (IllegalArgumentException e) {
            assertEquals("Start date cannot be after end date.", e.getMessage(), "Should throw an exception for start date after end date");
        }
    }
}
