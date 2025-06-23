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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Calendar;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import it.unipd.dei.softplat.http.service.HttpClientService;
import it.unipd.dei.softplat.monitoring.controller.MonitoringController;
import it.unipd.dei.softplat.monitoring.model.MonitoringRequest;
import it.unipd.dei.softplat.testutil.TestAsyncConfig;

/**
 * This class is intended to test the MonitoringService.
 * It contains test methods to validate the functionality of the MonitoringController and the MonitoringRequest model.
 */
@SpringBootTest
@Import(TestAsyncConfig.class)
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
                eq("http://datamanager-service:8082/datamanager/save-articles/"),
                anyString()
            )
        ).thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));
        when(httpClientService.postRequest(
            eq("http://client-service:8080/client/status/"),
            anyString()
            )
        ).thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));

        // Example of a valid request
        MonitoringRequest request = new MonitoringRequest();
        request.setissueString("example issue query");
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

        // To check if the API usage is exceeded
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(httpClientService, atLeastOnce()).postRequest(eq("http://client-service:8080/client/status/"), bodyCaptor.capture());

        // Parse the request body to check if the API usage is exceeded
        // I need to find: {"message":"API rate limit exceeded for query: example issue query","status":"MONITORING"}
        boolean found = bodyCaptor.getAllValues().stream().anyMatch(
            body -> body.contains("\"status\":\"MONITORING\"") &&
                    body.contains("\"message\":\"API rate limit exceeded for query: example issue query\"")
        );

        // Verify that the postRequest method of MonitoringService was called with the correct parameters
        if (!found) {
            verify(httpClientService, atLeastOnce()).postRequest(eq("http://datamanager-service:8082/datamanager/save-articles/"), anyString());
        } else {
            System.out.println("API usage limit exceeded!");
        }

        // Example of an invalid request (start date null)
        MonitoringRequest invalidRequest = new MonitoringRequest("example issue query", "example label", null, null);
        
        // Call the startMonitoring method with an invalid request
        ResponseEntity <?> invalidResponse = controller_test.startMonitoring(invalidRequest);
        
        // Assert that the response is not null and has a status code of 400 BAD REQUEST
        assertNotNull(invalidResponse, "Invalid response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, invalidResponse.getStatusCode(), "Invalid response should have status code 400 BAD REQUEST");

        // Example of an invalid request (issue string empty and label empty)
        MonitoringRequest invalidRequest2 = new MonitoringRequest("", "", startDate, endDate);

        // Call the startMonitoring method with an invalid request
        ResponseEntity <?> invalidResponse2 = controller_test.startMonitoring(invalidRequest2);

        // Assert that the response is not null and has a status code of 400 BAD REQUEST
        assertNotNull(invalidResponse2, "Invalid response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, invalidResponse2.getStatusCode(), "Invalid response should have status code 400 BAD REQUEST");

        // Example of invalid request (issue string null and label null)
        MonitoringRequest invalidRequest3 = new MonitoringRequest(null, null, startDate, endDate);

        // Call the startMonitoring method with an invalid request
        ResponseEntity <?> invalidResponse3 = controller_test.startMonitoring(invalidRequest3);

        // Assert that the response is not null and has a status code of 400 BAD REQUEST
        assertNotNull(invalidResponse3, "Invalid response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, invalidResponse3.getStatusCode(), "Invalid response should have status code 400 BAD REQUEST");
    }

    /**
     * This test method is intended to test the validation of the MonitoringRequest object.
     * It checks all the getters and setters of the MonitoringRequest class.
     */
    @Test
    public void testMonitoringRequestValidationGetterSetter() {
        MonitoringRequest request = new MonitoringRequest();

        // Set the issue query
        request.setissueString("test issue query");
        assertEquals("test issue query", request.getissueString(), "Issue query should match the set value");
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
    public void testMonitoringRequestValidationNullValues() {
        MonitoringRequest request = new MonitoringRequest();
        
        // Assert that the setters throw IllegalArgumentException when null values are passed
        assertThrows(IllegalArgumentException.class, () -> request.setissueString(null), "Expected exception for null issue query");
        assertThrows(IllegalArgumentException.class, () -> request.setLabel(null), "Expected exception for null label");
        assertThrows(IllegalArgumentException.class, () -> request.setStartDate(null), "Expected exception for null start date");
    }

    /**
     * This test method is intended to test the validation of the MonitoringRequest object for empty values.
     * It checks if the issue query, label, start date, and end date are not empty strings.
     * If any of these values are empty, it should throw an IllegalArgumentException.
     */
    @Test
    public void testMonitoringRequestValidationEmptyValues() {
        MonitoringRequest request = new MonitoringRequest();
        
        // Assert that the setters throw IllegalArgumentException when empty values are passed
        assertThrows(IllegalArgumentException.class, () -> request.setissueString(""), "Expected exception for empty issue query");
        assertThrows(IllegalArgumentException.class, () -> request.setLabel(""), "Expected exception for empty label");
    }

    /**
     * This test method is intended to test the validation of the MonitoringRequest object for date order.
     * It checks if the start date is before the end date and vice versa.
     * If the dates are in the wrong order, it should throw an IllegalArgumentException.
     */
    @Test
    public void testMonitoringRequestValidationDateOrder() {
        MonitoringRequest request = new MonitoringRequest();
        MonitoringRequest request2 = new MonitoringRequest();
        
        Calendar cal_01_01_2023 = Calendar.getInstance();
        cal_01_01_2023.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
        Calendar cal_02_01_2023 = Calendar.getInstance();
        cal_02_01_2023.set(2023, Calendar.JANUARY, 2, 23, 59, 59);
        Date endDate = cal_01_01_2023.getTime();
        Date startDate = cal_02_01_2023.getTime();
        Date endDate2 = cal_01_01_2023.getTime();
        Date startDate2 = cal_02_01_2023.getTime(); 
        
        // Assert that the setters throw IllegalArgumentException when end date is before start date
        request.setStartDate(startDate);
        assertThrows(IllegalArgumentException.class, () -> request.setEndDate(endDate), "Expected exception for end date before start date");
        
        // Assert that the setters throw IllegalArgumentException when start date is after end date
        request2.setEndDate(endDate2);
        assertThrows(IllegalArgumentException.class, () -> request2.setStartDate(startDate2), "Expected exception for start date after end date");
    }
}
