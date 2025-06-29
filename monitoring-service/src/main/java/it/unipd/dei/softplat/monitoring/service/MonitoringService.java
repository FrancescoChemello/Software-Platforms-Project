/**
 * MonitoringController.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.monitoring.service;

import java.util.ArrayList;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

// Client Java for The Guardian Open Platform API
import com.apitheguardian.GuardianContentApi;
import com.apitheguardian.bean.Article;
import com.apitheguardian.bean.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import it.unipd.dei.softplat.http.service.HttpClientService;
import it.unipd.dei.softplat.monitoring.model.MonitoringRequest;

/**
 * This class is intended to handle monitoring operations,
 * specifically fetching articles from The Guardian Open Platform API
 * based on a given issue query and date range.
 */
@Service
public class MonitoringService {

    private GuardianContentApi client;
    private String apiKey;
    @Value("${data.batch.size}")
    private int batchSize;
    @Value("${init.sleep.time}")
    int initSleepTime;
    @Value("${increment.sleep.time}")
    int incrementSleepTime;
    private final HttpClientService httpClientService;

    // For logging
    private static final Logger logger = LogManager.getLogger(MonitoringService.class);
    
    public MonitoringService(@Value("${guardian.open.api.key}") String apiKey, HttpClientService httpClientService) {

        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("The Guardian Service API environment variable is not set.");
        }
        this.client = new GuardianContentApi(apiKey);
        this.apiKey = apiKey;
        this.httpClientService = httpClientService;
    }

    /**
     * Starts the monitoring process for the given request.
     * @param request
     */
    @Async
    public void startMonitoring(MonitoringRequest request) {

        Response responseTheGuardian;
        ArrayList<JSONObject> retrievedArticles = new ArrayList<>();

        // Variable to check if the monitoring status has been sent to the Client Service
        boolean monitoringStatusSent = false;

        // Object mapper for JSON processing
        ObjectMapper mapper = new ObjectMapper();

        int totalArticles = 0;
        
        if (request == null) {
            throw new IllegalArgumentException("Monitoring request cannot be null.");
        }
        
        if (request.getEndDate() == null) {
            logger.info("End date is null, monitoring will continue indefinitely.");
        } else {
            logger.info("Monitoring completed for the given date range.");
        }

        // Variable to check if the monitoring should continue indefinitely
        boolean continueMonitoring = (request.getEndDate() == null);
        
        Date startDate = request.getStartDate();
        Date endDate;
        
        // Loop the section
        do {
            if (!continueMonitoring) {
                // If the end date is not null, set the end date for the query
                endDate = request.getEndDate();
            } else {
                // If the end date is null, set the end date to the current date
                endDate = new Date(); // new Date() = current date and time 
            }
            // Set the date range for the query
            client.setFromDate(startDate);
            client.setToDate(endDate); 

            // Set the label for the query
            try{
                responseTheGuardian = client.getContent(request.getissueString());
            }
            catch (UnirestException e){
                // Check if the exception is due to API rate limit exceeded
                Throwable cause = e.getCause();
                boolean found = false;
                while (!found) {
                    if (cause.getMessage().contains("API rate limit exceeded")) {
                        found = true; // Stop searching for the cause
                        break;
                    }
                    cause = cause.getCause();
                }
                // If it is caused by API limit, log the error and stop monitoring
                if (found) {
                    logger.error("API rate limit exceeded. Stopping monitoring.");
                    continueMonitoring = false; // Stop monitoring if an error occurs
                    sendStatusToClientService("MONITORING", "API rate limit exceeded", request.getissueString());
                    return; // Exit
                }
                logger.error("Error while fetching content from The Guardian API: " + e.getMessage(), e);
                return; // Exit
            }

            ArrayList<Article> articles = new ArrayList<>();

            int pageAttempts = 0;
            int articleAttempts = 0;
            int sleepTime = initSleepTime;

            /**
             * Note: 
             * The Guardian API has a limit of 500 requests per day and a maximum of 1 request per second.
             * I keep 50 requests as a buffer to retry some requests in case of errors.
             * So, the maximum number of requests per day is 450, and each page has 10 articles.
             * Therefore, the maximum number of pages is:
             *      450 / (10 requests for articles + 1 request for the page) = 40 pages (rounded down). 
             */
            // TODO: Change the number of pages to 2 for testing purposes
            // Loop to retrieve articles from all pages
            for (int page = 1; page <= (request.getissueString().equals("example issue query") ? Math.min(2, responseTheGuardian.getPages()) : (responseTheGuardian.getTotal() <= 400 ? responseTheGuardian.getPages() : 40)); page++) {
            // for (int page = 1; page <= (request.getissueString().equals("example issue query") ? Math.min(2, responseTheGuardian.getPages()) : responseTheGuardian.getPages()); page++) {
            // for (int page = 1; page <= Math.min(2, responseTheGuardian.getPages()); page++) {
                HttpResponse<JsonNode> response = null;
                try {
                    // Query the page [page]
                    response = Unirest.get("https://content.guardianapis.com/search")
                            .queryString("q", request.getissueString())
                            .queryString("from-date", startDate.toInstant().toString())
                            .queryString("to-date", endDate.toInstant().toString())
                            .queryString("page", page)
                            .queryString("api-key", this.apiKey)
                            .asJson();
                }
                catch (UnirestException e) {
                    // Check if the exception is due to API rate limit exceeded
                    Throwable cause = e.getCause();
                    boolean found = false;
                    while (!found) {
                        if (cause.getMessage().contains("API rate limit exceeded")) {
                            found = true; // Stop searching for the cause
                            break;
                        }
                        cause = cause.getCause();
                    }
                    // If it is caused by API limit, log the error and stop monitoring
                    if (found) {
                        logger.error("API rate limit exceeded. Stopping monitoring.");
                        continueMonitoring = false; // Stop monitoring if an error occurs
                        sendStatusToClientService("MONITORING", "API rate limit exceeded", request.getissueString());
                        return; // Exit
                    }
                    logger.error("Error while fetching articles from The Guardian at page " + page + ": " + e.getMessage(), e);
                    return; // Exit
                }
                
                // Sleep for a while to avoid hitting the API rate limit
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                // Check if I did too many attempts to fetch articles from a page
                if (pageAttempts >= 5 && response != null && response.getStatus() == 429) {
                    if (response != null && response.getBody().toString().contains("API rate limit exceeded")) {
                        logger.error("API rate limit exceeded. Stopping monitoring.");
                        continueMonitoring = false; // Stop monitoring if an error occurs
                        sendStatusToClientService("MONITORING", "API rate limit exceeded", request.getissueString());
                        return; // Exit
                    }
                    logger.error("Unable to fetch articles from The Guardian at page: " + page + " due to too many requests. Status: " + response.getStatus());
                    logger.error("Skipping page: " + page);
                    pageAttempts = 0; // Reset the attempts counter
                    // Wait some times
                    try {
                        Thread.sleep(1500); // Sleep for 2 seconds before retrying
                    } catch (InterruptedException e) {
                        logger.error("Retry interrupted: " + e.getMessage());
                        Thread.currentThread().interrupt(); // Restore the interrupted status
                    }
                    continue; // Skip to the next page
                }
                
                // Check if I reached the limit of requests per day
                if (pageAttempts < 5 && response != null && response.getStatus() == 429) {
                    if (response != null && response.getBody().toString().contains("API rate limit exceeded")) {
                        logger.error("API rate limit exceeded. Stopping monitoring.");
                        continueMonitoring = false; // Stop monitoring if an error occurs
                        sendStatusToClientService("MONITORING", "API rate limit exceeded", request.getissueString());
                        return; // Exit
                    }
                    logger.warn("Too much attempt. Status: " + response.getStatus());
                    pageAttempts++;
                    // Increase the sleep time to avoid hitting the API rate limit
                    sleepTime += incrementSleepTime * Math.pow(2, pageAttempts); // Increase the sleep time by incrementSleepTime milliseconds * 2^pageAttempts
                    // Wait for a while before retrying
                    try {
                        Thread.sleep(1500); // Sleep for 2 seconds before retrying
                    } catch (InterruptedException e) {
                        logger.error("Retry interrupted: " + e.getMessage());
                        Thread.currentThread().interrupt(); // Restore the interrupted status
                    }
                    // Retry the request for the same page
                    page--;
                    continue;
                }

                // Check if the response is null or if the status is not sleepTime
                if (response == null || response.getStatus() != 200) {
                    logger.error("Failed to fetch articles from The Guardian at page: " + page + ". Status: " + (response != null ? response.getStatus() : "No response received"));
                    continue; // Skip to the next page if the request failed
                }

                // No error, so reset the attempts counter
                if (response != null && response.getStatus() == 200){
                    pageAttempts = 0;
                    sleepTime = Math.max(initSleepTime, sleepTime - incrementSleepTime); // Decrease the sleep time by incrementSleepTime milliseconds if it is greater than initSleepTime milliseconds  
                }

                // Parse the response
                JSONArray results = response.getBody().getObject().getJSONObject("response").getJSONArray("results");
                // Check if the results are empty
                if (results.isEmpty()) {
                    logger.warn("No articles found for the given query and date range at page: " + page);
                    continue; // Skip to the next page if no articles are found
                }

                // Convert the results to an array to Article objects
                for (int i = 0; i < results.length(); i++) {
                    JSONObject articleJson = results.getJSONObject(i);
                    Article theGuradianArticle = new Article();
                    try {
                        theGuradianArticle = mapper.readValue(articleJson.toString(), Article.class);
                    } catch (JsonMappingException e) {
                        logger.error("Error mapping JSON to Article object at index " + i + ": " + e.getMessage());
                    } catch (JsonProcessingException e) {
                        logger.error("Error processing JSON to Article object at index " + i + ": " + e.getMessage());
                    }
                    // Check if the article is null
                    if (theGuradianArticle == null) {
                        logger.warn("Error during the conversion of article at index " + i + " to Article object.");
                        continue; // Skip to the next article if the conversion failed
                    } else {
                        // Append to the articles array
                        articles.add(theGuradianArticle);
                    }

                }

                for (int i = 0; i < articles.size(); i++) {
                    Article article = articles.get(i);
                    /**
                     * The article is as follows:
                     * {
                     *   "id": "article-id",
                     *   "type": "article_type",
                     *   "sectionId": "section_id",
                     *   "sectionName": "section_name",
                     *   "webPublicationDate": "2023-10-01T12:00:00Z",
                     *   "webTitle": "Article Title", 
                     *   "webUrl": "https://www.theguardian.com/article-url",
                     *   "apiUrl": "https://content.guardianapis.com/article-id",
                     *   "isHosted": false
                     * }
                     */
                    
                    String bodyText;

                    String apiurl = article.getApiUrl();
                    if( apiurl == null || apiurl.isEmpty()) {
                        logger.warn("Article API URL is missing.");
                        continue;
                    }

                    // Fetch the full article content using the API URL
                    try {
                        /**
                         * JSON response:
                         * Full article: 
                         * {
                         *  "response":
                         *  {
                         *      "userTier":"developer",
                         *      "total":1,
                         *      "content":{
                         *          "sectionName":"World news",
                         *          "pillarName":"News",
                         *          "webPublicationDate":"2023-12-27T03:46:38Z",
                         *          "apiUrl":"https://content.guardianapis.com/world/2023/dec/27/daihatsu-suspends-production-in-japan-after-safety-test-scandal",
                         *          "webUrl":"https://www.theguardian.com/world/2023/dec/27/daihatsu-suspends-production-in-japan-after-safety-test-scandal",
                         *          "isHosted":false,
                         *          "pillarId":"pillar/news",
                         *          "webTitle":"Daihatsu suspends production in Japan after safety test scandal",
                         *          "id":"world/2023/dec/27/daihatsu-suspends-production-in-japan-after-safety-test-scandal",
                         *          "sectionId":"world",
                         *          "type":"article",
                         *          "fields":{
                         *              "bodyText":"Production was suspended at the last operating domestic factory of Japanese automaker Daihatsu on Tuesday, as the ... 
                         *          }
                         *      },
                         *      "status":"ok"
                         *  }
                         * }
                         */
                        HttpResponse<JsonNode> fullArticle = Unirest.get(apiurl).queryString("api-key", this.apiKey).queryString("show-fields", "bodyText").asJson();
                        // Sleep for a while to avoid hitting the API rate limit
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }

                        // Check if I did too many attempts to fetch the full article content
                        if (articleAttempts >= 5 && response != null && response.getStatus() == 429) {
                            if (response != null && response.getBody().toString().contains("API rate limit exceeded")) {
                                logger.error("API rate limit exceeded. Stopping monitoring.");
                                continueMonitoring = false; // Stop monitoring if an error occurs
                                sendStatusToClientService("MONITORING", "API rate limit exceeded", request.getissueString());
                                return; // Exit
                            }
                            logger.error("Unable to fetch the content of an article from The Guardian due to too many requests. Status: " + response.getStatus());
                            logger.error("Skipping article: " + article.getId());
                            articleAttempts = 0; // Reset the attempts counter
                            // Wait some times
                            try {
                                Thread.sleep(1500); // Sleep for 2 seconds before retrying
                            } catch (InterruptedException e) {
                                logger.error("Retry interrupted: " + e.getMessage());
                                Thread.currentThread().interrupt(); // Restore the interrupted status
                            }
                            continue; // Skip to the next page
                        }

                        // Check if I reached the limit of requests per day
                        if (articleAttempts < 5 && fullArticle != null && fullArticle.getStatus() == 429) {
                            if (response != null && response.getBody().toString().contains("API rate limit exceeded")) {
                                logger.error("API rate limit exceeded. Stopping monitoring.");
                                continueMonitoring = false; // Stop monitoring if an error occurs
                                sendStatusToClientService("MONITORING", "API rate limit exceeded", request.getissueString());
                                return; // Exit
                            }
                            logger.warn("Too much attempt. Status: " + fullArticle.getStatus());
                            articleAttempts++;
                            sleepTime += incrementSleepTime * Math.pow(2, articleAttempts); // Increase the sleep time by incrementSleepTime milliseconds * 2^articleAttempts
                            // Wait for a while before retrying
                            try {
                                Thread.sleep(1500); // Sleep for 2 seconds before retrying
                            } catch (InterruptedException e) {
                                logger.error("Retry interrupted: " + e.getMessage());
                                Thread.currentThread().interrupt(); // Restore the interrupted status
                            }
                            // Retry the request for the same article
                            i--;
                            continue;
                        }
                        
                        if (fullArticle == null || fullArticle.getStatus() != 200) {
                            logger.error("Failed to fetch full article content. Status: " + (fullArticle != null ? fullArticle.getStatus() : "No response received"));
                            continue;
                        }

                        // No error, so reset the attempts counter
                        if (fullArticle != null && fullArticle.getStatus() == 200){
                            articleAttempts = 0;
                            sleepTime = Math.max(initSleepTime, sleepTime - incrementSleepTime); // Decrease the sleep time by incrementSleepTime milliseconds if it is greater than initSleepTime milliseconds
                        }

                        // Get the body text from the response
                        JSONObject body = fullArticle.getBody().getObject().getJSONObject("response").getJSONObject("content").getJSONObject("fields");
                        bodyText = body.getString("bodyText");
                        if (bodyText == null || bodyText.isEmpty()) {
                            logger.error("Body text is missing for article ID: " + article.getId());
                            continue;
                        }
                    } catch (UnirestException e) {
                        // Check if the exception is due to API rate limit exceeded
                        Throwable cause = e.getCause();
                        boolean found = false;
                        while (!found) {
                            if (cause.getMessage().contains("API rate limit exceeded")) {
                                found = true; // Stop searching for the cause
                                break;
                            }
                            cause = cause.getCause();
                        }
                        // If it is caused by API limit, log the error and stop monitoring
                        if (found) {
                            logger.error("API rate limit exceeded. Stopping monitoring.");
                            continueMonitoring = false; // Stop monitoring if an error occurs
                            sendStatusToClientService("MONITORING", "API rate limit exceeded", request.getissueString());
                            return; // Exit
                        }
                        logger.error("Error fetching full article content: " + e.getMessage());
                        continue;
                    }

                    // JSON format of the article
                    JSONObject articleJson = new JSONObject();
                    /**
                     * Saving:
                     * {
                     *   "id": "article-id",
                     *   "issueString": "query",
                     *   "label": "label"
                     *   "type": "article_type",
                     *   "sectionId": "section_id",
                     *   "sectionName": "section_name",
                     *   "webPublicationDate": "2023-10-01T12:00:00Z",
                     *   "webTitle": "Article Title",
                     *   "webUrl": "https://www.theguardian.com/article-url",
                     *   "bodyText": "Full article body text",
                     * }
                     */
                    articleJson.put("id", article.getId());
                    articleJson.put("issueString", request.getissueString());
                    articleJson.put("label", request.getLabel());
                    articleJson.put("type", article.getType());
                    articleJson.put("sectionId", article.getSectionId());
                    articleJson.put("sectionName", article.getSectionName());
                    articleJson.put("webPublicationDate", article.getWebPublicationDate());
                    articleJson.put("webTitle", article.getWebTitle());
                    articleJson.put("webUrl", article.getWebUrl());
                    articleJson.put("bodyText", bodyText);

                    retrievedArticles.add(articleJson);
                    logger.info("Article <" + article.getId() + "> retrieved: " + article.getWebTitle() + " at page " + page);
                }
                // Check if the retrievedArticles list has reached the batch size
                if (retrievedArticles.size() >= batchSize) {
                    logger.info("Batch size reached (" + retrievedArticles.size() + "), sending articles to DataManager Service.");
                    totalArticles += retrievedArticles.size();
                    // Send the articles to the DataManager Service
                    sendArticlesToDataManager(retrievedArticles);
                    // Send the status to the Client Service
                    if (!monitoringStatusSent) {
                        monitoringStatusSent = sendStatusToClientService("MONITORING", "Monitoring completed" , request.getissueString());
                    }
                    retrievedArticles.clear(); // Clear the list after sending
                }
                // Reset the articles list for the next page
                articles.clear();
            }

            // If there are still articles left in the retrievedArticles list, send them to the DataManager Service
            if (!retrievedArticles.isEmpty()) {
                logger.info("Sending remaining articles (" + retrievedArticles.size() + ") to DataManager Service.");
                totalArticles += retrievedArticles.size();
                sendArticlesToDataManager(retrievedArticles);
                // Send the status to the Client Service
                if (!monitoringStatusSent) {
                    monitoringStatusSent = sendStatusToClientService("MONITORING", "Monitoring completed" , request.getissueString());
                }
                retrievedArticles.clear(); // Clear the list after sending
            }

            logger.info("Retrieved " + totalArticles + " articles for the query: " + request.getissueString() + " from " + startDate + " to " + endDate);
                
            // Sleep for a while before the next monitoring cycle
            if (continueMonitoring) {
                // Clear the total articles count for the next cycle
                totalArticles = 0;
                logger.info("End a monitoring cycle, waiting for the next cycle to start.");
                // Set startDate
                startDate = endDate; // Set the end date to the current date
                try {
                    Thread.sleep(300000); // Sleep for 5 minutes before the next monitoring cycle
                } catch (InterruptedException e) {
                    logger.error("Monitoring interrupted: " + e.getMessage());
                    Thread.currentThread().interrupt(); // Restore the interrupted status
                }
            }
        } while (continueMonitoring);
        logger.info("Monitoring process completed for query: " + request.getissueString());
    }

    /**
     * Sends the retrieved articles to the DataManager Service in batches.
     * If the DataManager Service is not available, it will retry up to 5 times.
     * @param retrievedArticles
     */
    public void sendArticlesToDataManager(ArrayList<JSONObject> retrievedArticles) {        
        // Take the first batchSize articles from the retrievedArticles list
        ArrayList<JSONObject> articleBatch = new ArrayList<>(retrievedArticles.subList(0, Math.min(batchSize, retrievedArticles.size())));
        JSONArray articleBatchJsonArray = new JSONArray(articleBatch);
        // Send the batch of articles to the DataManager Service
        ResponseEntity<String> responseDataManager = httpClientService.postRequest("http://datamanager-service:8082/datamanager/save-articles/", articleBatchJsonArray.toString());
        if (responseDataManager != null && responseDataManager.getStatusCode() == HttpStatus.OK) {
            logger.info("Batch of articles sent to DataManager Service successfully.");
            // Remove the sent articles from the retrievedArticles list
            retrievedArticles.removeAll(articleBatch);
        } else {
            // If it fails, the array is not cleared and the next iteration will try to send the same set of articles plus a new one again
            logger.warn("Failed to send batch of articles to DataManager Service. Status: " + (responseDataManager != null ? responseDataManager.getStatusCode() : "No response received"));
        }

        // Send the JSON articles left to the DataManager Service
        int attempts = 0;
        while (!retrievedArticles.isEmpty() && attempts < 5) {
            // Take the first batchSize articles from the retrievedArticles list
            articleBatch = new ArrayList<>(retrievedArticles.subList(0, Math.min(batchSize, retrievedArticles.size())));
            articleBatchJsonArray = new JSONArray(articleBatch);
            responseDataManager = httpClientService.postRequest("http://datamanager-service:8082/datamanager/save-articles/", articleBatchJsonArray.toString());
            if (responseDataManager != null && responseDataManager.getStatusCode() == HttpStatus.OK) {
                logger.info("Batch of articles sent to DataManager Service successfully after " + (attempts + 1) + " attempts.");
                // Remove the sent articles from the retrievedArticles list
                retrievedArticles.removeAll(articleBatch);
            } else {
                attempts++;
                // Sleep for a while before retrying
                try {
                    Thread.sleep(1500 * attempts); // Sleep for 2 * attempts seconds before retrying
                } catch (InterruptedException e) {
                    logger.error("Retry interrupted: " + e.getMessage());
                    Thread.currentThread().interrupt(); // Restore the interrupted status
                }
                logger.warn("Failed to send batch of articles to DataManager Service. Status: " + (responseDataManager != null ? responseDataManager.getStatusCode() : "No response received"));
            }
        }
        // Check if some articles are left
        if (!retrievedArticles.isEmpty()) {
            logger.error("Some articles were not sent to the DataManager Service.");
        }
    }

    public boolean sendStatusToClientService(String status, String message, String query) {
        JSONObject monitoringCompletion = new JSONObject();
        monitoringCompletion.put("status", status);
        monitoringCompletion.put("message", message + " for query: " + query);
        ResponseEntity<String> responseClientService = httpClientService.postRequest("http://client-service:8080/client/status/", monitoringCompletion.toString());
        if (responseClientService != null && responseClientService.getStatusCode() == HttpStatus.OK) {
            logger.info("Monitoring status sent to Client Service successfully.\nStatus: " + status + "\nMessage: " + message + " for query: " + query);
            return true;
        } else {
            logger.warn("Failed to send monitoring status to Client Service. Status: " + (responseClientService != null ? responseClientService.getStatusCode() : "No response received"));
            int attempts = 0;
            while (attempts < 5) {
                attempts++;
                // Sleep for a while before retrying
                try {
                    Thread.sleep(1500 * attempts); // Sleep for 2 * attempts seconds before retrying
                } catch (InterruptedException e) {
                    logger.error("Retry interrupted: " + e.getMessage());
                    Thread.currentThread().interrupt(); // Restore the interrupted status
                }
                responseClientService = httpClientService.postRequest("http://client-service:8080/client/status/", monitoringCompletion.toString());
                if (responseClientService != null && responseClientService.getStatusCode() == HttpStatus.OK) {
                    logger.info("Monitoring status sent to Client Service successfully after " + (attempts + 1) + " attempts.\nStatus: " + status + "\nMessage: " + message + " for query: " + query);
                    return true;
                } else {
                    logger.warn("Failed to send monitoring status to Client Service. Status: " + (responseClientService != null ? responseClientService.getStatusCode() : "No response received"));
                }
            }
        }
        return false;
    }
}
