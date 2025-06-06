/**
 * DataManagerService.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.datamanager.service;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import it.unipd.dei.softplat.datamanager.model.Article;
import it.unipd.dei.softplat.datamanager.model.ArticleTopics;
import it.unipd.dei.softplat.http.service.HttpClientService;

/**
 * This class is intended to handle data management operations,
 * specifically storing articles in MongoDB and Elasticsearch.
 * It processes a list of articles, prepares them for storage,
 * and sends them in batches to the respective services.
 */
@Service
public class DataManagerService {
    
    @Value("${data.batch.size}")
    private int batchSize;
    private final HttpClientService httpClientService;

    /**
     * Default constructor for DataManagerService.
     * @param httpClientService
     */
    public DataManagerService(HttpClientService httpClientService){
        this.httpClientService = httpClientService;
    }

    /**
     * This method receives a list of articles from the Monitoring service
     * and processes them for topic extraction using the Mallet Service.
     * @param articles
     */
    public void topicExtraction(List<Article> articles) {
        ArrayList<JSONObject> malletArticles = new ArrayList<>();

        for (Article article : articles) {
            if (article == null) {
                System.out.println("Received null article.");
            } else {
                // Prepare the article for Mallet topic extraction
                JSONObject malletArticle = new JSONObject();
                malletArticle.put("id", article.getId());
                malletArticle.put("type", article.getType());
                malletArticle.put("sectionID", article.getSectionId());
                malletArticle.put("sectionName", article.getSectionName());
                malletArticle.put("webPublicationDate", article.getWebPublicationDate());
                malletArticle.put("webTitle", article.getWebTitle());
                malletArticle.put("webUrl", article.getWebUrl());
                malletArticle.put("bodyText", article.getBodyText());

                malletArticles.add(malletArticle);

                if (malletArticles.size() >= batchSize) {
                    // TODO: Implement a mechanism to process only a size of batchSize articles
                    // Create a MalletArticleDTO object to send the articles
                    JSONObject malletArticleDTO = new JSONObject();
                    malletArticleDTO.put("articles", new JSONArray(malletArticles));
                    malletArticleDTO.put("collectionName", article.getIssueQuery());
                    // Indicate that this is not the end of the articles
                    malletArticleDTO.put("end", false);
                    // Send articles to the Mallet Service
                    ResponseEntity<String> responseMallet = httpClientService.postRequest("http://localhost:8080/mallet/accumulate/", malletArticleDTO.toString());
                    if (responseMallet.getStatusCode() == HttpStatus.OK) {
                        System.out.println("Batch of articles sent to Mallet Service successfully.");
                        malletArticles.clear(); // Clear the list after sending
                    } else {
                        // If it fails, the array is not cleared and the next iteration will try to send the same set of articles plus a new one again
                        System.out.println("Failed to send batch of articles to Mallet Service. Status: " + responseMallet.getStatusCode());
                    }
                }
            }
        }
        // Send the JSON articles left to the Mallet Service
        if (!malletArticles.isEmpty()) {
            // Create a MalletArticleDTO object to send the articles
            JSONObject malletArticleDTO = new JSONObject();
            malletArticleDTO.put("articles", new JSONArray(malletArticles));
            malletArticleDTO.put("collectionName", articles.get(0).getIssueQuery());
            // Indicate that this is not the end of the articles
            malletArticleDTO.put("end", false); 
            // Send articles to the Mallet Service
            ResponseEntity<String> responseMallet = httpClientService.postRequest("http://localhost:8080/mallet/accumulate/", malletArticleDTO.toString());
            if (responseMallet.getStatusCode() == HttpStatus.OK) {
                System.out.println("Batch of articles sent to Mallet Service successfully.");
                malletArticles.clear(); // Clear the list after sending
            } else {
                // TODO: If it fails, I should try again to send the same set of articles using a while loop + a sleep
                System.out.println("Failed to send batch of articles to Mallet Service. Status: " + responseMallet.getStatusCode());
            }
        }
        // After sending all articles, I notify the Mallet Service that no other articles will be sent
        JSONObject endMalletArticleDTO = new JSONObject();
        endMalletArticleDTO.put("articles", new JSONArray());
        endMalletArticleDTO.put("collectionName", articles.get(0).getIssueQuery());
        // Indicate that this is the end of the articles
        endMalletArticleDTO.put("end", true);
        ResponseEntity<String> responseEndMallet = httpClientService.postRequest("http://localhost:8080/mallet/end/", endMalletArticleDTO.toString());
        if (responseEndMallet.getStatusCode() == HttpStatus.OK) {
            System.out.println("End of articles sent to Mallet Service successfully.");
        } else {
            // TODO: If it fails, I should try again to send the end notification using a while loop + a sleep
            System.out.println("Failed to send end notification to Mallet Service. Status: " + responseEndMallet.getStatusCode());
        }
    }

    /**
     * This method receives a list of articles with topics extracted
     * and stores them in MongoDB and Elasticsearch.
     * @param articles
     */
    public void storingArticles(List<ArticleTopics> articles){

        ArrayList<JSONObject> mongoArticles = new ArrayList<>();
        ArrayList<JSONObject> elasticArticles = new ArrayList<>();

        for (ArticleTopics article : articles) {
            if(article == null) {
                System.out.println("Received null article.");
            } else {
                // Prepare MongoArticle and ElasticArticle to be processed later
                JSONObject mngArticle = new JSONObject();
                mngArticle.put("id", article.getId());
                mngArticle.put("type", article.getType());
                mngArticle.put("sectionID", article.getSectionId());
                mngArticle.put("sectionName", article.getSectionName());
                mngArticle.put("webPublicationDate", article.getWebPublicationDate());
                mngArticle.put("webTitle", article.getWebTitle());
                mngArticle.put("webUrl", article.getWebUrl());
                mngArticle.put("bodyText", article.getBodyText());
                JSONObject elArticle = new JSONObject();
                elArticle.put("id", article.getId());
                elArticle.put("issueQuery", article.getIssueQuery());
                elArticle.put("label", article.getLabel());
                elArticle.put("type", article.getType());
                elArticle.put("webPublicationDate", article.getWebPublicationDate());
                elArticle.put("topics", new JSONArray(article.getTopics()));

                mongoArticles.add(mngArticle);
                elasticArticles.add(elArticle);

                // Send the batch of articles to the MongoDB Service
                if (mongoArticles.size() >= batchSize) {
                    // TODO: Implement a mechanism to process only a size of batchSize articles
                    // Create a SaveArticleDTO object to send the articles
                    JSONObject saveArticleDTO = new JSONObject();
                    saveArticleDTO.put("articles", new JSONArray(mongoArticles));
                    saveArticleDTO.put("collectionName", article.getIssueQuery());
                    // Send articles to the MongoDB Service
                    ResponseEntity<String> responseMongoDB = httpClientService.postRequest("http://localhost:8080/mongodb/save/", saveArticleDTO.toString());
                    if (responseMongoDB.getStatusCode() == HttpStatus.OK) {
                        System.out.println("Batch of articles sent to MongoDB Service successfully.");
                        mongoArticles.clear(); // Clear the list after sending
                    } else {
                        // If it fails, the array is not cleared and the next iteration will try to send the same set of articles plus a new one again
                        System.out.println("Failed to send batch of articles to MongoDB Service. Status: " + responseMongoDB.getStatusCode());
                    }
                }
                // Send the batch of articles to the ElastiSearch Service
                if (elasticArticles.size() >= batchSize) {
                    // TODO: Implement a mechanism to process only a size of batchSize articles
                    // Send articles to the ElastiSearch Service
                    ResponseEntity<String> responseElasticSearch = httpClientService.postRequest("http://localhost:8080/elastic/index/", elasticArticles.toString());
                    if (responseElasticSearch.getStatusCode() == HttpStatus.OK) {
                        System.out.println("Batch of articles sent to ElastiSearch Service successfully.");
                        elasticArticles.clear(); // Clear the list after sending
                    } else {
                        // If it fails, the array is not cleared and the next iteration will try to send the same set of articles plus a new one again
                        System.out.println("Failed to send batch of articles to ElastiSearch Service. Status: " + responseElasticSearch.getStatusCode());
                    }
                }
            }
        }
        // Send the JSON articles left to the MongoDB Service
        if(!mongoArticles.isEmpty()) {
            // Create a SaveArticleDTO object to send the articles
            JSONObject saveArticleDTO = new JSONObject();
            saveArticleDTO.put("articles", new JSONArray(mongoArticles));
            saveArticleDTO.put("collectionName", articles.get(0).getIssueQuery()); // Assuming all articles have the same issueQuery
            // Send articles to the MongoDB Service
            ResponseEntity<String> responseMongoDB = httpClientService.postRequest("http://localhost:8080/mongodb/save/", saveArticleDTO.toString());
            if (responseMongoDB.getStatusCode() == HttpStatus.OK) {
                System.out.println("Batch of articles sent to MongoDB Service successfully.");
                mongoArticles.clear(); // Clear the list after sending
            } else {
                // TODO: If it fails, I should try again to send the same set of articles using a while loop + a sleep
                System.out.println("Failed to send batch of articles to MongoDB Service. Status: " + responseMongoDB.getStatusCode());
            }
        }
        // Send articles left to the ElasticSearch Service
        if(!elasticArticles.isEmpty()) {
            // Send articles to the ElastiSearch Service
            ResponseEntity<String> responseElasticSearch = httpClientService.postRequest("http://localhost:8080/elastic/index/", elasticArticles.toString());
            if (responseElasticSearch.getStatusCode() == HttpStatus.OK) {
                System.out.println("Batch of articles sent to ElastiSearch Service successfully.");
                elasticArticles.clear(); // Clear the list after sending
            } else {
                // TODO: If it fails, I should try again to send the same set of articles using a while loop + a sleep
                System.out.println("Failed to send batch of articles to ElastiSearch Service. Status: " + responseElasticSearch.getStatusCode());
            }
        }
    }
}
