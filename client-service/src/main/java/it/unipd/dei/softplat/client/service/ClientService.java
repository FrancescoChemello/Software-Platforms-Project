/**
 * ClientService.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.client.service;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import it.unipd.dei.softplat.http.service.HttpClientService;
import it.unipd.dei.softplat.client.model.QueryTopic;

@Service
public class ClientService {

    private final HttpClientService httpClientService;
    boolean isMonitoringEnabled;
    
    /**
     * Default constructor for ClientService.
     * @param httpClientService
     */
    public ClientService(HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
        this.isMonitoringEnabled = false;
    }

    /**
     * Sends a monitoring request to the Monitoring service.
     * @param issueString
     */
    public void sendMonitoringRequest(String issueString) {
        // Send the monitoring request to the Monitoring service.
        ResponseEntity<String> response = httpClientService.postRequest("http://localhost:8080/monitoring/start/", issueString);
        if (response != null && response.getStatusCode() == HttpStatus.OK) {
            System.out.println("Monitoring request sent successfully.");
        } else {
            // Handle the case where the request failed.
            int attempts = 0;
            while (attempts < 5) {
                try {
                    Thread.sleep(2000 * attempts); // Wait for 2s * attempts before retrying
                    response = httpClientService.postRequest("http://localhost:8080/monitoring/start/", issueString);
                    if (response != null && response.getStatusCode() == HttpStatus.OK) {
                        System.out.println("Monitoring request sent successfully.");
                        break;
                    } else {
                        attempts++;
                        System.out.println("Failed to send monitoring request. Response: " + (response != null ? response.getBody() : "No response received"));
                    }
                } 
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status
                    System.out.println("Thread was interrupted while waiting to retry.");
                }
            }
        }
    }

    /**
     * Sends a query request to the Mallet service.
     * This method prepares a JSON object with the query parameters and sends it to the Mallet service.
     * @param query
     * @param corpus
     * @param subCorpus
     * @param numTopics
     * @param numTopWordsPerTopic
     * @param startDate
     * @param endDate
     */
    public void sendQueryRequest(String query, String corpus, String subCorpus, Integer numTopics, Integer numTopWordsPerTopic, Date startDate, Date endDate) {
        if (isMonitoringEnabled) {
            // Prepare the query request
            JSONObject queryRequest = new JSONObject();
            queryRequest.put("query", query);
            queryRequest.put("corpus", corpus);
            queryRequest.put("subCorpus", subCorpus);
            queryRequest.put("numTopics", numTopics);
            queryRequest.put("numTopWordsPerTopic", numTopWordsPerTopic);
            queryRequest.put("startDate", startDate.toString());
            queryRequest.put("endDate", endDate.toString());
            // Send the query request to the Mallet service
            ResponseEntity<String> response = httpClientService.postRequest("http://localhost:8080/mallet/search/", queryRequest.toString());
            if (response != null && response.getStatusCode() == HttpStatus.OK) {
                System.out.println("Query request sent successfully for query: " + query);
            } else {
                // Handle the case where the request failed.
                int attempts = 0;
                while (attempts < 5) {
                    try {
                        Thread.sleep(2000 * attempts); // Wait for 2s * attempts before retrying
                        response = httpClientService.postRequest("http://localhost:8080/mallet/search/", queryRequest.toString());
                        if (response != null && response.getStatusCode() == HttpStatus.OK) {
                            System.out.println("Query request sent successfully for query: " + query);
                            break;
                        } else {
                            attempts++;
                            System.out.println("Failed to send query request. Response: " + (response != null ? response.getBody() : "No response received"));
                        }
                    } 
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Restore interrupted status
                        System.out.println("Thread was interrupted while waiting to retry.");
                    }
                }
            }
        } else {
            System.out.println("Monitoring is not enabled. Cannot send query request.");
        }
    }

    /**
     * Processes the query result received from the client.
     * This method will print the results to the console.
     * @param query
     * @param topics
     */
    public void processQueryResult(String query, ArrayList<QueryTopic> topics) {
        System.out.println("Result for query: " + query);
        if (topics.isEmpty()) {
            System.out.println("No articles found for the query: " + query);
        } else {
            System.out.println("Found " + topics.size() + " articles for the query: " + query);
            for (QueryTopic topic : topics) {
                System.out.println("Article ID: " + topic.getId());
                System.out.println("Top words: " + String.join(", ", topic.getTopWords()));
            }
        }
    }

    /**
     * Checks if monitoring is enabled based on the status and message received from the client.
     * If the status is "MONITORING", it sets the isMonitoringEnabled flag to true and prints a message.
     * @param status
     * @param message
     */
    public void isMonitoringEnabled(String status, String message) {
        if (status.equals("MONITORING")) {
            // Monitoring is enabled
            isMonitoringEnabled = true;
            System.out.println("Monitoring is enabled. Message: " + message);
        }
    }
}
