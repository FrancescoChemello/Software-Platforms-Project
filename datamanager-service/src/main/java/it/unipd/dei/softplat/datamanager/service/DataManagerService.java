package it.unipd.dei.softplat.datamanager.service;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;

import it.unipd.dei.softplat.datamanager.model.Article;

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
                        HttpResponse<String> responseMongoDB = Unirest.post("http://localhost:8080/mongodb-service/").header("Content-Type", "application/json").body(mongoArticles.toString()).asString();
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
                        HttpResponse<String> responseElasticSearch = Unirest.post("http://localhost:8080/elastic-service/").header("Content-Type", "application/json").body(elasticArticles.toString()).asString();
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
                HttpResponse<String> responseMongoDB = Unirest.post("http://localhost:8080/mongodb-service/").header("Content-Type", "application/json").body(mongoArticles.toString()).asString();
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
                HttpResponse<String> responseElasticSearch = Unirest.post("http://localhost:8080/elastic-service/").header("Content-Type", "application/json").body(elasticArticles.toString()).asString();
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
