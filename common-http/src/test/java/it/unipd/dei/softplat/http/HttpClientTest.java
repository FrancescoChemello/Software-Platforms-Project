/**
 * HttpClientTest.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import it.unipd.dei.softplat.http.service.HttpClientService;

@ExtendWith(MockitoExtension.class)
public class HttpClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private HttpClientService httpClientService;

    @Test
    public void testPostRequest() {
        // Create a valid URL and JSON body
        String url = "http://client-service:8080/";
        String jsonBody = "{\"key\":\"value\"}";
        ResponseEntity<String> mockResponse = ResponseEntity.ok("ok");
        
        // Configure Mock
        org.mockito.Mockito.when(
            restTemplate.postForEntity(eq(url), org.mockito.ArgumentMatchers.any(), eq(String.class))
        ).thenReturn(mockResponse);
        
        // Call the postRequest method
        ResponseEntity<String> response = httpClientService.postRequest(url, jsonBody);

        // Check that the response is not null
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ok", response.getBody());
        
        // Verify that the postRequest method is called
        verify(restTemplate).postForEntity(eq(url), any(), eq(String.class));
    }

    @Test
    public void testPostRequest_nullUrl() {
        ResponseEntity<String> response = httpClientService.postRequest(null, "{\"foo\":\"bar\"}");
        
        // Check if the response is not null and has the expected status code
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error: URL cannot be null or empty", response.getBody());
    }

    @Test
    public void testPostRequest_exception() {
        String url = "http://client-service:8080/test";
        String jsonBody = "{\"foo\":\"bar\"}";

        org.mockito.Mockito.when(
            restTemplate.postForEntity(eq(url), org.mockito.ArgumentMatchers.any(), eq(String.class))
        ).thenThrow(new org.springframework.web.client.HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));

        ResponseEntity<String> response = httpClientService.postRequest(url, jsonBody);

        // Check if the response is not null and has the expected status code
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Verifica che RestTemplate sia stato chiamato
        verify(restTemplate).postForEntity(eq(url), org.mockito.ArgumentMatchers.any(), eq(String.class));
    }

    @Test
    public void testGetRequest() {
        // Create a valid URL
        String url = "http://client-service:8080/test";
        ResponseEntity<String> mockResponse = ResponseEntity.ok("ok");

        // Configure Mock
        org.mockito.Mockito.when(
            restTemplate.getForEntity(eq(url), eq(String.class))
        ).thenReturn(mockResponse);

        // Call the getRequest method
        ResponseEntity<String> response = httpClientService.getRequest(url);
        
        // Check that the response is not null and has the expected status code
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ok", response.getBody());

        // Verify that the getRequest method is called
        verify(restTemplate).getForEntity(eq(url), eq(String.class));
    }

    @Test
    public void testGetRequest_nullUrl() {
        ResponseEntity<String> response = httpClientService.getRequest(null);
        
        // Check if the response is not null and has the expected status code
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error: URL cannot be null or empty", response.getBody());
    }

    @Test
    public void testGetRequest_exception() {
        String url = "http://client-service:8080/test";

        org.mockito.Mockito.when(
            restTemplate.getForEntity(eq(url), eq(String.class))
        ).thenThrow(new org.springframework.web.client.HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));

        ResponseEntity<String> response = httpClientService.getRequest(url);
        
        // Check if the response is not null and has the expected status code
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Verify that the getRequest method is called
        verify(restTemplate).getForEntity(eq(url), eq(String.class));
    }
}
