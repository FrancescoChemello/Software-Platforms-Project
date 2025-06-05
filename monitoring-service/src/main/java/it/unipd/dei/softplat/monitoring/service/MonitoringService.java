/**
 * MonitoringController.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.monitoring.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public void startMonitoring(MonitoringRequest request) {

        Response response;
        ArrayList<JSONObject> retrievedArticles = new ArrayList<>();

        if (request == null) {
            throw new IllegalArgumentException("Monitoring request cannot be null.");
        }

        // To manage date formats
        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        try {
            client.setFromDate(dateFormat.parse(request.getStartDate()));
            client.setToDate(dateFormat.parse(request.getEndDate()));
        }
        catch (ParseException e){
            throw new IllegalArgumentException("Invalid date format. Please use dd/MM/yyyy.");
        }

        // Set the label for the query
        try{
            response = client.getContent(request.getIssueQuery());
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
                        // System.out.println("Full article: " + fullArticle.getBody().toString());
                        JSONObject body = fullArticle.getBody().getObject().getJSONObject("response").getJSONObject("content").getJSONObject("fields");
                        bodyText = body.getString("bodyText");
                        if (bodyText == null || bodyText.isEmpty()) {
                            System.out.println("Body text is missing for article ID: " + article.getId());
                            continue;
                        }
                        System.out.println("Body Text: " + bodyText);
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
                     *   "issueQuery": "query",
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
                    articleJson.put("issueQuery", request.getIssueQuery());
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

                    // Send the JSON articles to the DataManager Service
                    if (retrievedArticles.size() >= batchSize) {
                        // TODO: Implement a mechanism to process only a size of batchSize articles
                        // Send the batch of articles to the DataManager Service
                        ResponseEntity<String> responseDataManager = httpClientService.postRequest("http://localhost:8080/articles/", retrievedArticles.toString());
                        if (responseDataManager.getStatusCode() == HttpStatus.OK) {
                            System.out.println("Batch of articles sent to DataManager Service successfully.");
                            retrievedArticles.clear(); // Clear the list after sending
                        } else {
                            // If it fails, the array is not cleared and the next iteration will try to send the same set of articles plus a new one again
                            System.out.println("Failed to send batch of articles to DataManager Service. Status: " + responseDataManager.getStatusCode());
                        }
                    }
                }
            }
            // Send the JSON articles left to the DataManager Service
            if (!retrievedArticles.isEmpty()) {
                ResponseEntity<String> responseDataManager = httpClientService.postRequest("http://localhost:8080/articles/", retrievedArticles.toString());
                if (responseDataManager.getStatusCode() == HttpStatus.OK) {
                    System.out.println("Batch of articles sent to DataManager Service successfully.");
                    retrievedArticles.clear(); // Clear the list after sending
                } else {
                    // TODO: If it fails, I should try again to send the same set of articles using a while loop + a sleep
                    System.out.println("Failed to send batch of articles to DataManager Service. Status: " + responseDataManager.getStatusCode());
                }
            }
        }
    }
}
