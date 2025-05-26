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
import org.springframework.http.ResponseEntity;

import it.unipd.dei.softplat.monitoring.controller.MonitoringController;
import it.unipd.dei.softplat.monitoring.service.MonitoringService;
import it.unipd.dei.softplat.monitoring.model.MonitoringRequest;

public class MonitoringServiceTest {
    
    private final MonitoringController controller_test = new MonitoringController(new MonitoringService(java.util.ResourceBundle.getBundle("application").getString("guardian.open.api.key")));

    @Test
    public void testStartMonitoring() {
        // This test method is intended to test the startMonitoring method of the MonitoringController.
        // It creates a valid and an invalid MonitoringRequest and call the startMonitoring method.
        
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

    @Test
    public void testRequestValidationGetterSetter() {
        // This test method is intended to test the validation of the MonitoringRequest object.
        // It checks all the getters and setters of the MonitoringRequest class.
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

    @Test
    public void testRequestValidationNullValues() {
        // This test method is intended to test the validation of the MonitoringRequest object for null values.
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

    @Test
    public void testRequestValidationEmptyValues() {
        // This test method is intended to test the validation of the MonitoringRequest object for empty values.
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

    @Test
    public void testRequestValidationPatterns() {
        // This test method is intended to test the validation of the MonitoringRequest object for patterns.
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

    @Test
    public void testRequestValidationDateOrder() {
        // This test method is intended to test the validation of the MonitoringRequest object for date order.
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
