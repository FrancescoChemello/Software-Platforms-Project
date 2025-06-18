/**
 * DataManagerService.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.datamanager.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import it.unipd.dei.softplat.datamanager.model.Article;
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
    
    // For logging
    private static final Logger logger = LogManager.getLogger(DataManagerService.class);

    /**
     * Default constructor for DataManagerService.
     * @param httpClientService
     */
    public DataManagerService(HttpClientService httpClientService){
        this.httpClientService = httpClientService;
    }

    /**
     * This method receives a list of articles with topics extracted
     * and stores them in MongoDB and Elasticsearch.
     * @param articles
     */
    public void storingArticles(List<Article> articles){

        ArrayList<JSONObject> mongoArticles = new ArrayList<>();
        ArrayList<JSONObject> elasticArticles = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

        for (Article article : articles) {
            if(article == null) {
                logger.warn("Received null article.");
            } else {
                // Prepare MongoArticle and ElasticArticle to be processed later
                JSONObject mngArticle = new JSONObject();
                mngArticle.put("id", article.getId());
                mngArticle.put("type", article.getType());
                mngArticle.put("sectionID", article.getSectionId());
                mngArticle.put("sectionName", article.getSectionName());
                mngArticle.put("webPublicationDate", formatter.format(article.getWebPublicationDate().toInstant()));
                mngArticle.put("webTitle", article.getWebTitle());
                mngArticle.put("webUrl", article.getWebUrl());
                mngArticle.put("bodyText", article.getBodyText());
                JSONObject elArticle = new JSONObject();
                elArticle.put("id", article.getId());
                elArticle.put("issueString", article.getissueString());
                elArticle.put("label", article.getLabel());
                elArticle.put("type", article.getType());
                elArticle.put("webPublicationDate", formatter.format(article.getWebPublicationDate().toInstant()));
                elArticle.put("webTitle", article.getWebTitle());
                elArticle.put("bodyText", article.getBodyText());

                mongoArticles.add(mngArticle);
                elasticArticles.add(elArticle);
            }
        }
        // Send the batch of articles to the MongoDB Service
        if (mongoArticles.size() >= batchSize || !mongoArticles.isEmpty()) {
            // Take the first batchSize articles from the mongoArticles list
            ArrayList<JSONObject> articleBatch = new ArrayList<>(mongoArticles.subList(0, Math.min(batchSize, mongoArticles.size())));
            // Create a SaveArticleDTO object to send the articles
            JSONObject saveArticleDTO = new JSONObject();
            saveArticleDTO.put("articles", new JSONArray(articleBatch));
            saveArticleDTO.put("collectionName", articles.get(0).getLabel());
            // Send articles to the MongoDB Service
            ResponseEntity<String> responseMongoDB = httpClientService.postRequest("http://mongodb-service:8085/mongodb/save/", saveArticleDTO.toString());
            if (responseMongoDB != null && responseMongoDB.getStatusCode() == HttpStatus.OK) {
                logger.info("Batch of articles sent to MongoDB Service successfully.");
                // Remove the sent articles from the mongoArticles list
                mongoArticles.removeAll(articleBatch);
            } else {
                // If it fails, the array is not cleared and the next iteration will try to send the same set of articles plus a new one again
                logger.warn("Failed to send batch of articles to MongoDB Service. Status: " + (responseMongoDB != null ? responseMongoDB.getStatusCode() : "No response received"));
            }
        }
        // Send the batch of articles to the ElastiSearch Service
        if (elasticArticles.size() >= batchSize || !elasticArticles.isEmpty()) {
            // Take the first batchSize articles from the mongoArticles list
            ArrayList<JSONObject> articleBatch = new ArrayList<>(elasticArticles.subList(0, Math.min(batchSize, elasticArticles.size())));
            // Create an IndexArticleDTO object to send the articles
            JSONObject IndexArticleDTO = new JSONObject();
            IndexArticleDTO.put("articles", new JSONArray(articleBatch));
            IndexArticleDTO.put("collectionName", articles.get(0).getLabel());
            // Send articles to the ElastiSearch Service
            ResponseEntity<String> responseElasticSearch = httpClientService.postRequest("http://elasticsearch-service:8083/elastic/index/", IndexArticleDTO.toString());
            if (responseElasticSearch != null && responseElasticSearch.getStatusCode() == HttpStatus.OK) {
                logger.info("Batch of articles sent to ElastiSearch Service successfully.");
                // Remove the sent articles from the mongoArticles list
                elasticArticles.removeAll(articleBatch);
            } else {
                // If it fails, the array is not cleared and the next iteration will try to send the same set of articles plus a new one again
                logger.warn("Failed to send batch of articles to ElastiSearch Service. Status: " + (responseElasticSearch != null ? responseElasticSearch.getStatusCode() : "No response received"));
            }
        }

        // Send the JSON articles left to the MongoDB Service
        int attempts = 0;
        while (!mongoArticles.isEmpty() && attempts < 5) {
            // Take the first batchSize articles from the mongoArticles list
            ArrayList<JSONObject> articleBatch = new ArrayList<>(mongoArticles.subList(0, Math.min(batchSize, mongoArticles.size())));
            // Create a SaveArticleDTO object to send the articles
            JSONObject saveArticleDTO = new JSONObject();
            saveArticleDTO.put("articles", new JSONArray(articleBatch));
            saveArticleDTO.put("collectionName", articles.get(0).getissueString()); // Assuming all articles have the same issueString
            // Send articles to the MongoDB Service
            ResponseEntity<String> responseMongoDB = httpClientService.postRequest("http://mongodb-service:8085/mongodb/save/", saveArticleDTO.toString());
            if (responseMongoDB != null && responseMongoDB.getStatusCode() == HttpStatus.OK) {
                logger.info("Batch of articles sent to MongoDB Service successfully after " + (attempts + 1) + " attempts.");
                // Remove the sent articles from the mongoArticles list
                mongoArticles.removeAll(articleBatch);
            } else {
                attempts++;
                // Sleep for a while before retrying
                try {
                    Thread.sleep(2000 * attempts); // Sleep for 2 * attempts seconds before retrying
                } catch (InterruptedException e) {
                    logger.error("Retry interrupted: " + e.getMessage());
                    Thread.currentThread().interrupt(); // Restore the interrupted status
                }
                logger.warn("Failed to send batch of articles to MongoDB Service. Status: " + (responseMongoDB != null ? responseMongoDB.getStatusCode() : "No response received"));
            }
        }
        // Check if some articles are left
        if (!mongoArticles.isEmpty()) {
            logger.error("Some articles were not sent to the MongoDB Service.");
        } else {
            logger.info("All articles sent to the MongoDB Service successfully.");
        }

        // Send articles left to the ElasticSearch Service
        attempts = 0;
        while (!elasticArticles.isEmpty() && attempts < 5) {
            // Take the first batchSize articles from the elasticArticles list
            ArrayList<JSONObject> articleBatch = new ArrayList<>(elasticArticles.subList(0, Math.min(batchSize, elasticArticles.size())));
            // Create an IndexArticleDTO object to send the articles
            JSONObject IndexArticleDTO = new JSONObject();
            IndexArticleDTO.put("articles", new JSONArray(articleBatch));
            IndexArticleDTO.put("collectionName", articles.get(0).getissueString());
            // Send articles to the ElastiSearch Service
            ResponseEntity<String> responseElasticSearch = httpClientService.postRequest("http://elasticsearch-service:8083/elastic/index/", IndexArticleDTO.toString());
            if (responseElasticSearch != null && responseElasticSearch.getStatusCode() == HttpStatus.OK) {
                logger.info("Batch of articles sent to ElastiSearch Service successfully after " + (attempts + 1) + " attempts.");
                // Remove the sent articles from the elasticArticles list
                elasticArticles.removeAll(articleBatch);
            } else {
                attempts++;
                // Sleep for a while before retrying
                try {
                    Thread.sleep(2000 * attempts); // Sleep for 2 * attempts seconds before retrying
                } catch (InterruptedException e) {
                    logger.error("Retry interrupted: " + e.getMessage());
                    Thread.currentThread().interrupt(); // Restore the interrupted status
                }
                logger.warn("Failed to send batch of articles to ElastiSearch Service. Status: " + (responseElasticSearch != null ? responseElasticSearch.getStatusCode() : "No response received"));
            }
        }
        // Check if some articles are left
        if (!elasticArticles.isEmpty()) {
            logger.error("Some articles were not sent to the Elasticsearch Service.");
        } else {
            logger.info("All articles sent to the Elasticsearch Service successfully.");
        }
    }
}
