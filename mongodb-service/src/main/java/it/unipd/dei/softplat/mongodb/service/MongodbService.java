/**
 * MongodbService.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.mongodb.service;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReplaceOptions;

import it.unipd.dei.softplat.mongodb.model.MongoArticle;
import it.unipd.dei.softplat.http.service.HttpClientService;

/**
 * This class is intended to handle MongoDB operations.
 * It provides methods to establish a connection to MongoDB, create and drop collections,
 * retrieve collections, and save articles to MongoDB.
 */
@Service
public class MongodbService {

    private MongoDatabase database;
    private MongoClient mongoClient;
    @Value("${data.batch.size}")
    private int batchSize;
    private final HttpClientService httpClientService;
 
    /**
     * Default constructor for MongodbService.
     * @param mongoClient
     * @param httpClientService
     */
    @Autowired
    public MongodbService(MongoClient mongoClient, HttpClientService httpClientService) {
        this.mongoClient = mongoClient;
        this.database = mongoClient.getDatabase("softplatDB");
        this.httpClientService = httpClientService;
    }

    /**
     * This method is responsible for closing the MongoDB connection.
     */
    public void closeConnection() {
        if (this.mongoClient != null) {
            this.mongoClient.close();
            System.out.println("MongoDB connection closed.");
        } else {
            System.out.println("No MongoDB connection to close.");
        }
    }

    /**
     * This method is responsible for creating a collection in MongoDB.
     * @param collectionName
     * @throws Exception if there is an error creating the collection.
     */
    private void createCollection(String collectionName) {
        try {
            // Create the collection with a unique index on the "id" field
            database.createCollection(collectionName);
            MongoCollection<Document> collection = database.getCollection(collectionName);
            collection.createIndex(Indexes.ascending("id"), new IndexOptions().unique(true));
            System.out.println("Collection " + collectionName + " created successfully.");
        }
        catch (Exception e) {
            System.out.println("Error creating collection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * This method is responsible for dropping a collection in MongoDB.
     * @param collectionName
     * @throws Exception if there is an error dropping the collection.
     */
    private void dropACollection(String collectionName) {
        try {
            // Drop the collection
            database.getCollection(collectionName).drop();

            System.out.println("Collection " + collectionName + " dropped successfully.");
        }
        catch (Exception e) {
            System.out.println("Error dropping collection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * This method retrieves the list of collections in the MongoDB database.
     * @return A list of collection names.
     * @throws Exception if there is an error retrieving the collections.
     */
    private List<String> listCollections() {
        try {
             // Get the list of collections
            return database.listCollectionNames().into(new ArrayList<>());
        } catch (Exception e) {
            System.out.println("Error retrieving collections: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    } 

    /**
     * This method is responsible for saving articles to MongoDB.
     * @param articles The list of articles to be saved.
     */
    public void saveArticles(List<MongoArticle> articles, String collectionName) {
        // Check if the collection exists, if not create it
        if (!listCollections().contains(collectionName)) {
            createCollection(collectionName);
        }
        // Process articles
        for (MongoArticle article : articles) {
            if (article == null) {
                System.out.println("Received null article.");
            } else {
                // Prepare the article to be saved
                try {
                    // Save the article to the specified collection
                    MongoCollection<Document> collection = database.getCollection(collectionName);
                    // Convert the article to a Document
                    Document articleDoc = new Document();
                    articleDoc.append("id", article.getId())
                              .append("type", article.getType())
                              .append("sectionID", article.getSectionId())
                              .append("sectionName", article.getSectionName())
                              .append("webPublicationDate", article.getWebPublicationDate())
                              .append("webTitle", article.getWebTitle())
                              .append("webUrl", article.getWebUrl())
                              .append("bodyText", article.getBodyText());
                    // Insert the article into the collection using replaceOne with upsert option
                    // This will update the article if it exists, or insert it if it does not
                    collection.replaceOne(new Document("id", article.getId()), articleDoc, new ReplaceOptions().upsert(true));
                    System.out.println("Article with ID " + article.getId() + " saved successfully.");
                } catch (Exception e) {
                    System.out.println("Error saving article with ID " + article.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        System.out.println("All articles saved successfully to collection " + collectionName + ".");
    }

    /**
     * This method is responsible for dropping a collection in MongoDB.
     * It checks if the collection exists before attempting to drop it.
     * @param collectionName
     */
    public void dropCollection(String collectionName) {
        // Check if the collection exists
        if (!listCollections().contains(collectionName)) {
            System.out.println("Collection " + collectionName + " does not exist.");
            return;
        }
        // Drop the specified collection
        dropACollection(collectionName);
    }

    /**
     * This method retrieves articles from a specific collection in MongoDB
     * based on a list of article IDs.
     * @param collectionName
     * @param query
     * @param ids
     */
    public void getArticlesById(String collectionName, String query, List<String> ids) {
        List<JSONObject> articles = new ArrayList<>();
        // Check if the collection exists
        if (!listCollections().contains(collectionName)) {
            System.out.println("Collection " + collectionName + " does not exist.");
            return;
        }
        // Retrieve articles from the specified collection with a specific ID
        try {
            for (String id : ids) {
                Document doc = database.getCollection(collectionName).find(new Document("id", id)).first();
                // Check if the document exists
                if (doc == null) {
                    System.out.println("No article found with ID: " + id);
                    continue; // Skip to the next ID
                } 
                // Convert the Document (BSON) to a JSON file
                JSONObject article = new JSONObject();
                article.put("id", doc.getString("id"));
                article.put("type", doc.getString("type"));
                article.put("sectionID", doc.getString("sectionID"));
                article.put("sectionName", doc.getString("sectionName"));
                article.put("webPublicationDate", doc.getString("webPublicationDate"));
                article.put("webTitle", doc.getString("webTitle"));
                article.put("webUrl", doc.getString("webUrl"));
                article.put("bodyText", doc.getString("bodyText"));
                articles.add(article);

                // Send the article to the Mallet service
                if (articles.size() >= batchSize) {
                    // Take the first batchSize articles from the articles list
                    ArrayList<JSONObject> articleBatch = new ArrayList<>(articles.subList(0, Math.min(batchSize, articles.size())));
                    // Create an AccumulateMalletArticlesDTO object to send to the Mallet service
                    JSONObject accumulateMalletArticlesDTO = new JSONObject();
                    accumulateMalletArticlesDTO.put("articles", articleBatch);
                    accumulateMalletArticlesDTO.put("collectionName", collectionName);
                    accumulateMalletArticlesDTO.put("query", query);
                    accumulateMalletArticlesDTO.put("endOfStream", false);
                    // Send the batch of articles to the Mallet service
                    ResponseEntity<String> responseQuery = httpClientService.postRequest("http://mallet-service:8084/mallet/accumulate/", accumulateMalletArticlesDTO.toString());
                    if (responseQuery != null && responseQuery.getStatusCode() == HttpStatus.OK) {
                        System.out.println("Batch of articles sent to Client Service successfully.");
                        // Remove the sent articles from the articles list
                        articles.removeAll(articleBatch);
                    } else {
                        // If it fails, the array is not cleared and the next iteration will try to send the same set of articles plus a new one again
                        System.out.println("Failed to send batch of articles to Service Service. Status: " + (responseQuery != null ? responseQuery.getStatusCode() : "No response received"));
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println("Error retrieving articles from collection " + collectionName + ": " + e.getMessage());
            e.printStackTrace();
        }
        // Send the remaining articles if any
        int attempts = 0;
        while (!articles.isEmpty() && attempts < 5) {
            // Take the first batchSize articles from the articles list
            ArrayList<JSONObject> articleBatch = new ArrayList<>(articles.subList(0, Math.min(batchSize, articles.size())));
            // Create an AccumulateMalletArticlesDTO object to send to the Mallet service
            JSONObject accumulateMalletArticlesDTO = new JSONObject();
            accumulateMalletArticlesDTO.put("articles", articleBatch);
            accumulateMalletArticlesDTO.put("collectionName", collectionName);
            accumulateMalletArticlesDTO.put("query", query);
            accumulateMalletArticlesDTO.put("endOfStream", false);
            // Send the batch of articles to the Mallet service
            ResponseEntity<String> responseQuery = httpClientService.postRequest("http://mallet-service:8084/mallet/accumulate/", accumulateMalletArticlesDTO.toString());
            if (responseQuery != null && responseQuery.getStatusCode() == HttpStatus.OK) {
                System.out.println("Batch of articles sent to Mallet Service successfully.");
                // Remove the sent articles from the articles list
                articles.removeAll(articleBatch);
            } else {
                attempts++;
                // Sleep for a while before retrying
                try {
                    Thread.sleep(2000 * attempts); // Sleep for 2 * attempts seconds before retrying
                } catch (InterruptedException e) {
                    System.out.println("Retry interrupted: " + e.getMessage());
                    Thread.currentThread().interrupt(); // Restore the interrupted status
                }
                System.out.println("Failed to send batch of articles to Mallet Service. Status: " + (responseQuery != null ? responseQuery.getStatusCode() : "No response received"));
            }
        }
        // Check if some articles are left
        if (!articles.isEmpty()) {
            System.out.println("Some articles were not sent to the Mallet Service.");
        } else {
            // Send the end of stream signal to the Mallet service
            JSONObject endOfStreamDTO = new JSONObject();
            endOfStreamDTO.put("articles", new ArrayList<>());
            endOfStreamDTO.put("collectionName", collectionName);
            endOfStreamDTO.put("query", query);
            endOfStreamDTO.put("endOfStream", true);
            // Send the end of stream signal to the Mallet service
            ResponseEntity<String> responseEndOfStream = httpClientService.postRequest("http://mallet-service:8084/mallet/accumulate/", endOfStreamDTO.toString());
            if (responseEndOfStream != null && responseEndOfStream.getStatusCode() == HttpStatus.OK) {
                System.out.println("End of stream signal sent to Mallet Service successfully.");
            } else {
                attempts = 0;
                while (attempts < 5) {
                    // Retry sending the end of stream signal
                    responseEndOfStream = httpClientService.postRequest("http://mallet-service:8084/mallet/accumulate/", endOfStreamDTO.toString());
                    if (responseEndOfStream != null && responseEndOfStream.getStatusCode() == HttpStatus.OK) {
                        System.out.println("End of stream signal sent to Mallet Service successfully.");
                        break; // Exit the loop if successful
                    } else {
                        attempts++;
                        // Sleep for a while before retrying
                        try {
                            Thread.sleep(2000 * attempts); // Sleep for 2 * attempts seconds before retrying
                        } catch (InterruptedException e) {
                            System.out.println("Retry interrupted: " + e.getMessage());
                            Thread.currentThread().interrupt(); // Restore the interrupted status
                        }
                        System.out.println("Failed to send end of stream signal to Mallet Service. Status: " + (responseEndOfStream != null ? responseEndOfStream.getStatusCode() : "No response received"));
                    }
                }
            }
            System.out.println("All articles retrieved successfully from collection " + collectionName + ".");
        }
    }
}
