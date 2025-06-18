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

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;

// Client Java for The Guardian Open Platform API
import com.apitheguardian.GuardianContentApi;
import com.apitheguardian.bean.Article;
import com.apitheguardian.bean.Response;

import it.unipd.dei.softplat.monitoring.model.MonitoringRequest;
import it.unipd.dei.softplat.http.service.HttpClientService;

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
    private final HttpClientService httpClientService;
    
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

        Response response;
        ArrayList<JSONObject> retrievedArticles = new ArrayList<>();

        // Variable to check if the monitoring status has been sent to the Client Service
        boolean monitoringStatusSent = false;
        
        if (request == null) {
            throw new IllegalArgumentException("Monitoring request cannot be null.");
        }
        
        if (request.getEndDate() == null) {
            System.out.println("End date is null, monitoring will continue indefinitely.");
        } else {
            System.out.println("Monitoring completed for the given date range.");
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
                response = client.getContent(request.getissueString());
            }
            catch (UnirestException e){
                throw new RuntimeException("Error while fetching content from The Guardian API: " + e.getMessage(), e);
            }

            // Here you can implement the logic to process the response and store it in the database
            if (response.getResults().length == 0) {
                System.out.println("No articles found for the given query and date range.");
            } else {
                System.out.println("Found " + response.getResults().length + " articles.");
                // Convert the response results to a stream
                Article [] articles = response.getResults();
                // Second call to get the body of the articles
                for (Article article : articles) {
                    if (article == null) {
                        System.out.println("Received null article.");
                    } else {
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
                            System.out.println("Article API URL is missing.");
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
                            if (fullArticle.getStatus() != 200) {
                                System.out.println("Failed to fetch full article content. Status: " + fullArticle.getStatus());
                                continue;
                            }
                            // Get the body text from the response
                            JSONObject body = fullArticle.getBody().getObject().getJSONObject("response").getJSONObject("content").getJSONObject("fields");
                            bodyText = body.getString("bodyText");
                            if (bodyText == null || bodyText.isEmpty()) {
                                System.out.println("Body text is missing for article ID: " + article.getId());
                                continue;
                            }
                        } catch (UnirestException e) {
                            System.out.println("Error fetching full article content: " + e.getMessage());
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
                        System.out.println("Article processed: " + article.getId());
                    }
                }
                // Send the JSON articles to the DataManager Service
                if (retrievedArticles.size() >= batchSize || !retrievedArticles.isEmpty()) {
                    // Take the first batchSize articles from the retrievedArticles list
                    ArrayList<JSONObject> articleBatch = new ArrayList<>(retrievedArticles.subList(0, Math.min(batchSize, retrievedArticles.size())));
                    JSONArray articleBatchJsonArray = new JSONArray(articleBatch);
                    // Send the batch of articles to the DataManager Service
                    ResponseEntity<String> responseDataManager = httpClientService.postRequest("http://datamanager-service:8082/datamanager/save-articles/", articleBatchJsonArray.toString());
                    if (responseDataManager != null && responseDataManager.getStatusCode() == HttpStatus.OK) {
                        System.out.println("Batch of articles sent to DataManager Service successfully.");
                        // Remove the sent articles from the retrievedArticles list
                        retrievedArticles.removeAll(articleBatch);
                    } else {
                        // If it fails, the array is not cleared and the next iteration will try to send the same set of articles plus a new one again
                        System.out.println("Failed to send batch of articles to DataManager Service. Status: " + (responseDataManager != null ? responseDataManager.getStatusCode() : "No response received"));
                    }
                }
                // Send the JSON articles left to the DataManager Service
                int attempts = 0;
                while (!retrievedArticles.isEmpty() && attempts < 5) {
                    // Take the first batchSize articles from the retrievedArticles list
                    ArrayList<JSONObject> articleBatch = new ArrayList<>(retrievedArticles.subList(0, Math.min(batchSize, retrievedArticles.size())));
                    JSONArray articleBatchJsonArray = new JSONArray(articleBatch);
                    ResponseEntity<String> responseDataManager = httpClientService.postRequest("http://datamanager-service:8082/datamanager/save-articles/", articleBatchJsonArray.toString());
                    if (responseDataManager != null && responseDataManager.getStatusCode() == HttpStatus.OK) {
                        System.out.println("Batch of articles sent to DataManager Service successfully.");
                        // Remove the sent articles from the retrievedArticles list
                        retrievedArticles.removeAll(articleBatch);
                    } else {
                        attempts++;
                        // Sleep for a while before retrying
                        try {
                            Thread.sleep(2000 * attempts); // Sleep for 2 * attempts seconds before retrying
                        } catch (InterruptedException e) {
                            System.out.println("Retry interrupted: " + e.getMessage());
                            Thread.currentThread().interrupt(); // Restore the interrupted status
                        }
                        System.out.println("Failed to send batch of articles to DataManager Service. Status: " + (responseDataManager != null ? responseDataManager.getStatusCode() : "No response received"));
                    }
                }
                // Check if some articles are left
                if (!retrievedArticles.isEmpty()) {
                    System.out.println("Some articles were not sent to the DataManager Service.");
                }

                // Send to the Client Service that the monitoring is completed
                if (!monitoringStatusSent) {
                    JSONObject monitoringCompletion = new JSONObject();
                    monitoringCompletion.put("status", "MONITORING");
                    monitoringCompletion.put("message", "Monitoring completed for query: " + request.getissueString());
                    ResponseEntity<String> responseClientService = httpClientService.postRequest("http://client-service:8080/client/status/", monitoringCompletion.toString());
                    if (responseClientService != null && responseClientService.getStatusCode() == HttpStatus.OK) {
                        System.out.println("Monitoring status sent to Client Service successfully.");
                    } else {
                        System.out.println("Failed to send monitoring status to Client Service. Status: " + (responseClientService != null ? responseClientService.getStatusCode() : "No response received"));
                        attempts = 0;
                        while (attempts < 5) {
                            attempts++;
                            // Sleep for a while before retrying
                            try {
                                Thread.sleep(2000 * attempts); // Sleep for 2 * attempts seconds before retrying
                            } catch (InterruptedException e) {
                                System.out.println("Retry interrupted: " + e.getMessage());
                                Thread.currentThread().interrupt(); // Restore the interrupted status
                            }
                            responseClientService = httpClientService.postRequest("http://client-service:8080/client/status/", monitoringCompletion.toString());
                            if (responseClientService != null && responseClientService.getStatusCode() == HttpStatus.OK) {
                                System.out.println("Monitoring status sent to Client Service successfully.");
                                break;
                            } else {
                                System.out.println("Failed to send monitoring status to Client Service. Status: " + (responseClientService != null ? responseClientService.getStatusCode() : "No response received"));
                            }
                        }
                    }
                }
            }
            // Sleep for a while before the next monitoring cycle
            if (continueMonitoring) {
                // Set startDate
                startDate = endDate; // Set the end date to the current date
                try {
                    Thread.sleep(60000); // Sleep for 60 seconds before the next monitoring cycle
                } catch (InterruptedException e) {
                    System.out.println("Monitoring interrupted: " + e.getMessage());
                    Thread.currentThread().interrupt(); // Restore the interrupted status
                }
            }
        } while (continueMonitoring);
    }
}
