/**
 * HttpClientService.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.http.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;

/**
 * This class is a service for making HTTP requests using RestTemplate.
 * It provides methods to send POST and GET requests to specified URLs with JSON bodies.
 */
@Service
public class HttpClientService {
    
    /**
     * This RestTemplate instance is used to make HTTP requests.
     */
    private final RestTemplate restTemplate;

    /**
     * This constructor initializes the HttpClientService with a RestTemplate instance.
     * The RestTemplate is used to make HTTP requests.
     * @param restTemplate
     */
    @Autowired
    public HttpClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * This method is intended to send a POST request to a specified URL with a JSON body.
     * It uses the RestTemplate to create an HTTP request with the provided JSON body and headers.
     * @param url
     * @param JSONBody
     * @return the response status of the POST request
     * @throws HttpClientErrorException if the request fails
     */
    public ResponseEntity<String> postRequest(String url, String JSONBody) {
        // Checking if the URL is null or empty
        if (url == null || url.isEmpty()) {
            return new ResponseEntity<String>("Error: URL cannot be null or empty", HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<String> statusRequest;
        
        // Creating headers for the HTTP request
        HttpHeaders headers = new HttpHeaders();
        // Setting the content type to application/json
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Setting the request with the JSON body and headers
        HttpEntity<String> restRequest = new HttpEntity<>(JSONBody, headers);
        // Sending a POST request to the specified URL with the provided JSON body
        try {
            statusRequest = restTemplate.postForEntity(url, restRequest, String.class);
        }
        catch (HttpClientErrorException e) {
            System.out.println("Failed to send POST request to " + url + ". Error: " + e.getMessage());
            return new ResponseEntity<String>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return statusRequest;
    }

    /**
     * This method is intended to send a GET request to a specified URL.
     * It uses the RestTemplate to create an HTTP request to the provided URL.
     * @param url
     * @return the response status of the GET request
     * @throws HttpClientErrorException if the request fails
     */
    public ResponseEntity<String> getRequest(String url) {
        // Checking if the URL is null or empty
        if (url == null || url.isEmpty()) {
            return new ResponseEntity<>("Error: URL cannot be null or empty", HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<String> statusRequest;

        // Sending a GET request to the specified URL
        try {
            statusRequest = restTemplate.getForEntity(url, String.class);
        }
        catch (HttpClientErrorException e) {
            System.out.println("Failed to send GET request to " + url + ". Error: " + e.getMessage());
            return new ResponseEntity<String>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return statusRequest;
    }
}
