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
import org.springframework.stereotype.Service;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;

import it.unipd.dei.softplat.datamanager.model.Article;

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

    public DataManagerService(){ }

    public void storingArticles(List<Article> articles){

        ArrayList<JSONObject> mongoArticles = new ArrayList<>();
        ArrayList<JSONObject> elasticArticles = new ArrayList<>();

        for(Article article : articles) {
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

                mongoArticles.add(mngArticle);
                elasticArticles.add(elArticle);

                // Send the JSON article to the MongoDB Service
                if (mongoArticles.size() >= batchSize) {
                    // Send the batch of articles to the MongoDB Service
                    try {
                        // Create a SaveArticleDTO object to send the articles
                        JSONObject saveArticleDTO = new JSONObject();
                        saveArticleDTO.put("articles", new JSONArray(mongoArticles));
                        saveArticleDTO.put("collectionName", article.getIssueQuery());
                        HttpResponse<String> responseMongoDB = Unirest.post("http://localhost:8080/mongodb/save/").header("Content-Type", "application/json").body(saveArticleDTO.toString()).asString();
                        if (responseMongoDB.getStatus() == 200) {
                            System.out.println("Batch of articles sent to MongoDB Service successfully.");
                            mongoArticles.clear(); // Clear the list after sending
                        } else {
                            // If it fails, the array is not cleared and the next iteration will try to send the same set of articles plus a new one again
                            System.out.println("Failed to send batch of articles to MongoDB Service. Status: " + responseMongoDB.getStatus());
                        }
                    }
                    catch (UnirestException e) {
                        System.out.println("Error sending articles to MongoDB Service: " + e.getMessage());
                    }
                }
                // Send the JSON article to the ElastiSearch Service
                if (elasticArticles.size() >= batchSize) {
                    // Send the batch of articles to the ElastiSearch Service
                    try {
                        HttpResponse<String> responseElasticSearch = Unirest.post("http://localhost:8080/elastic/index/").header("Content-Type", "application/json").body(elasticArticles.toString()).asString();
                        if (responseElasticSearch.getStatus() == 200) {
                            System.out.println("Batch of articles sent to ElastiSearch Service successfully.");
                            elasticArticles.clear(); // Clear the list after sending
                        } else {
                            // If it fails, the array is not cleared and the next iteration will try to send the same set of articles plus a new one again
                            System.out.println("Failed to send batch of articles to ElastiSearch Service. Status: " + responseElasticSearch.getStatus());
                        }
                    }
                    catch (UnirestException e) {
                        System.out.println("Error sending articles to ElastiSearch Service: " + e.getMessage());
                    }
                }
            }
        }
        // Send the JSON articles left to the MongoDB Service
        if(!mongoArticles.isEmpty()) {
            // Send the batch of articles to the MongoDB Service
            try {
                // Create a SaveArticleDTO object to send the articles
                JSONObject saveArticleDTO = new JSONObject();
                saveArticleDTO.put("articles", new JSONArray(mongoArticles));
                saveArticleDTO.put("collectionName", articles.get(0).getIssueQuery()); // Assuming all articles have the same issueQuery
                HttpResponse<String> responseMongoDB = Unirest.post("http://localhost:8080/mongodb/save/").header("Content-Type", "application/json").body(saveArticleDTO.toString()).asString();
                if (responseMongoDB.getStatus() == 200) {
                    System.out.println("Batch of articles sent to MongoDB Service successfully.");
                    mongoArticles.clear(); // Clear the list after sending
                } else {
                    // TODO: If it fails, I should try again to send the same set of articles using a while loop + a sleep
                    System.out.println("Failed to send batch of articles to MongoDB Service. Status: " + responseMongoDB.getStatus());
                }
            }
            catch (UnirestException e) {
                System.out.println("Error sending articles to MongoDB Service: " + e.getMessage());
            }
        }
        // Send the JSON articles left to the ElasticSearch Service
        if(!elasticArticles.isEmpty()) {
            // Send the batch of articles to the ElastiSearch Service
            try {
                HttpResponse<String> responseElasticSearch = Unirest.post("http://localhost:8080/elastic/index/").header("Content-Type", "application/json").body(elasticArticles.toString()).asString();
                if (responseElasticSearch.getStatus() == 200) {
                    System.out.println("Batch of articles sent to ElastiSearch Service successfully.");
                    elasticArticles.clear(); // Clear the list after sending
                } else {
                    // TODO: If it fails, I should try again to send the same set of articles using a while loop + a sleep
                    System.out.println("Failed to send batch of articles to ElastiSearch Service. Status: " + responseElasticSearch.getStatus());
                }
            }
            catch (UnirestException e) {
                System.out.println("Error sending articles to ElastiSearch Service: " + e.getMessage());
            }
        }
    }
}
