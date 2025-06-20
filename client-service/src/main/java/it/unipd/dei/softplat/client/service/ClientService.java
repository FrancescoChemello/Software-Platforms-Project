/**
 * ClientService.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.client.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import it.unipd.dei.softplat.http.service.HttpClientService;
import it.unipd.dei.softplat.client.ClientApp;
import it.unipd.dei.softplat.client.model.QueryResult;
import it.unipd.dei.softplat.client.model.QueryTopic;

@Service
public class ClientService {

    private final HttpClientService httpClientService;
    boolean isMonitoringEnabled;
    boolean isApiRateLimitExceeded;

    // For logging
    private static final Logger logger = LogManager.getLogger(ClientService.class);
    
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
     * @param label
     * @param startDate
     * @param endDate
     */
    public void sendMonitoringRequest(String issueString, String label, Date startDate, Date endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        // JSON object to hold the monitoring request data
        JSONObject monitoringRequest = new JSONObject();
        monitoringRequest.put("issueString", issueString);
        monitoringRequest.put("label", label);
        monitoringRequest.put("startDate", formatter.format(startDate.toInstant()));
        if (endDate == null){
            monitoringRequest.put("endDate", endDate);
        } else {
            monitoringRequest.put("endDate", formatter.format(endDate.toInstant()));
        }
        // Send the monitoring request to the Monitoring service.
        ResponseEntity<String> response = httpClientService.postRequest("http://monitoring-service:8081/monitoring/start/", monitoringRequest.toString());
        if (response != null && response.getStatusCode() == HttpStatus.OK) {
            logger.info("Monitoring request sent successfully.");
        } else {
            // Handle the case where the request failed.
            int attempts = 0;
            while (attempts < 5) {
                try {
                    Thread.sleep(2000 * attempts); // Wait for 2s * attempts before retrying
                    response = httpClientService.postRequest("http://monitoring-service:8081/monitoring/start/", monitoringRequest.toString());
                    if (response != null && response.getStatusCode() == HttpStatus.OK) {
                        logger.info("Monitoring request sent successfully after " + (attempts + 1) + " attempts.");
                        break;
                    } else {
                        attempts++;
                        logger.warn("Failed to send monitoring request. Response: " + (response != null ? response.getBody() : "No response received"));
                    }
                } 
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status
                    logger.error("Thread was interrupted while waiting to retry.");
                }
            }
        }
    }

    /**
     * Sends a query request to the Mallet service.
     * This method prepares a JSON object with the query parameters and sends it to the Mallet service.
     * @param query
     * @param corpus
     * @param numTopics
     * @param numTopWordsPerTopic
     * @param startDate
     * @param endDate
     */
    public void sendQueryRequest(String query, String corpus, Integer numTopics, Integer numTopWordsPerTopic, Date startDate, Date endDate) {
        if (isMonitoringEnabled) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
            // Prepare the query request
            JSONObject queryRequest = new JSONObject();
            queryRequest.put("query", query);
            queryRequest.put("corpus", corpus);
            queryRequest.put("numTopics", numTopics);
            queryRequest.put("numTopWordsPerTopic", numTopWordsPerTopic);
            if (startDate == null) {
                queryRequest.put("startDate", startDate);
            } else {
                queryRequest.put("startDate", formatter.format(startDate.toInstant()));
            }
            if (endDate == null) {
                queryRequest.put("endDate", endDate);
            } else {
                queryRequest.put("endDate", formatter.format(endDate.toInstant()));
            }
            // Send the query request to the Mallet service
            ResponseEntity<String> response = httpClientService.postRequest("http://mallet-service:8084/mallet/search/", queryRequest.toString());
            if (response != null && response.getStatusCode() == HttpStatus.OK) {
                logger.info("Query request sent successfully for query: " + query);
            } else {
                // Handle the case where the request failed.
                int attempts = 0;
                while (attempts < 5) {
                    try {
                        Thread.sleep(2000 * attempts); // Wait for 2s * attempts before retrying
                        response = httpClientService.postRequest("http://mallet-service:8084/mallet/search/", queryRequest.toString());
                        if (response != null && response.getStatusCode() == HttpStatus.OK) {
                            logger.info("Query request sent successfully for query: " + query + " after " + (attempts + 1) + " attempts.");
                            break;
                        } else {
                            attempts++;
                            logger.warn("Failed to send query request. Response: " + (response != null ? response.getBody() : "No response received"));
                        }
                    } 
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Restore interrupted status
                        logger.error("Thread was interrupted while waiting to retry.");
                    }
                }
            }
        } else {
           logger.warn("Monitoring is not enabled. Cannot send query request.");
        }
    }

    /**
     * Processes the query result received from the client.
     * This method will print the results to the console.
     * @param query
     * @param topics
     */
    public void processQueryResult(String query, ArrayList<QueryTopic> topics) {
        logger.info("Processing query result for query: " + query);
        QueryResult result = new QueryResult(query, topics);
        try {
            ClientApp.resultQueue.put(result);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            logger.error("Thread was interrupted while trying to put the result in the queue.");
        }
        logger.info("Processed query result for query: " + query);
    }

    /**
     * Checks if monitoring is enabled based on the status and message received from the client.
     * This method will update the isMonitoringEnabled flag and notify the ClientApp if monitoring is enabled.
     * @param status
     * @param message
     */
    public void isMonitoringEnabled(String status, String message) {
        if (status.equals("MONITORING")) {
            if (message.contains("Monitoring completed"))
                // Monitoring is enabled
                isMonitoringEnabled = true;
                logger.info("Monitoring is enabled. Message: " + message);
                try {
                    // Notify the ClientApp that monitoring is now enabled
                    ClientApp.monitoringQueue.put("Monitoring is enabled");
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status
                    logger.error("Thread was interrupted while trying to put the message in the queue.");
                }
        } else {
            if(status.contains("API rate limit exceeded")){
                logger.error("API rate limit exceeded.");
                isApiRateLimitExceeded = true;
                // Unable to continue due to API rate limit
                try {
                    ClientApp.monitoringQueue.put("API rate limit exceeded");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status
                    logger.error("Thread was interrupted while trying to put the message in the queue.");
                }
            }
        }
    }

    /**
     * Checks if monitoring is enabled.
     * This method returns the value of the isMonitoringEnabled flag.
     * @return
     */
    public boolean monitoringStatus() {
        return isMonitoringEnabled;
    }

    /**
     * Checks if the API rate limit has been exceeded.
     * This method returns the value of the isApiRateLimitExceeded flag.
     * @return
     */
    public boolean apiRateLimitStatus() {
        return isApiRateLimitExceeded;
    }
}
