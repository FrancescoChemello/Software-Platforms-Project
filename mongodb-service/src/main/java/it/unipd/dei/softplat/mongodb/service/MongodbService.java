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
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import it.unipd.dei.softplat.mongodb.model.MongoArticle;

/**
 * This class is intended to handle MongoDB operations.
 * It provides methods to establish a connection to MongoDB, create and drop collections,
 * retrieve collections, and save articles to MongoDB.
 */
@Service
public class MongodbService {

    private String mongoUrl;
    private String mongoPort;
    private MongoDatabase database;
    private MongoClient mongoClient;
    @Value("${data.batch.size}")
    private int batchSize;
 
    /**
     * Default constructor for MongodbService.
     * @param mongoUrl
     * @param mongoPort
     */
    public MongodbService(@Value("${mongodb.url}") String mongoUrl, @Value("${mongodb.port}") String mongoPort) {
        this.mongoUrl = mongoUrl;
        this.mongoPort = mongoPort;
        // Establish connections to MongoDB
        try {
            establishConnections();
            System.out.println("Connected to MongoDB at " + mongoUrl + ":" + mongoPort);
        } catch (Exception e) {
            System.out.println("Error establishing connection to MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Default constructor for MongodbService.
     * This method establishes a connection to the MongoDB server.
     * @throws Exception if there is an error establishing the connection.
     */
    private void establishConnections() {
        try {
            // Establish a connection to the MongoDB server
            String connectionString = "mongodb://" + mongoUrl + ":" + mongoPort;
            System.out.println("Connecting to MongoDB at " + connectionString);
            // Create a MongoClient instance
            this.mongoClient = MongoClients.create(connectionString);
            // Get the database
            this.database = mongoClient.getDatabase("softplatDB");

            System.out.println("Connected to MongoDB at " + mongoUrl + ":" + mongoPort);
        }
        catch (Exception e) {
            System.out.println("Error establishing connection to MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
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
            // Establish a connection to MongoDB if not already done
            if (this.database == null) {
                establishConnections();
            }
            // Create the collection
            database.createCollection(collectionName);

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
            // Establish a connection to MongoDB if not already done
            if (this.database == null) {
                establishConnections();
            }
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
            // Establish a connection to MongoDB if not already done
            if (this.database == null) {
                establishConnections();
            }
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
        // Establish a connection to MongoDB if not already done
        if (this.database == null) {
            establishConnections();
        }
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
                    // Insert the article into the collection
                    collection.insertOne(articleDoc);
                    System.out.println("Article with ID " + article.getId() + " saved successfully.");
                } catch (Exception e) {
                    System.out.println("Error saving article with ID " + article.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        // Close connection after saving articles
        closeConnection();
        System.out.println("All articles saved successfully to collection " + collectionName + ".");
    }

    /**
     * This method is responsible for dropping a collection in MongoDB.
     * It checks if the collection exists before attempting to drop it.
     * @param collectionName
     */
    public void dropCollection(String collectionName) {
        // Establish a connection to MongoDB if not already done
        if (this.database == null) {
            establishConnections();
        }
        // Check if the collection exists
        if (!listCollections().contains(collectionName)) {
            System.out.println("Collection " + collectionName + " does not exist.");
            return;
        }
        // Drop the specified collection
        dropACollection(collectionName);

        // Close connection after dropping the collection
        closeConnection();
    }

    public void getArticlesById(String collectionName, List<String> ids) {
        List<JSONObject> articles = new ArrayList<>();
        // Establish a connection to MongoDB if not already done
        if (this.database == null) {
            establishConnections();
        }
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

                // Send the article to the Query service
                if (articles.size() >= batchSize) {
                    // Send the batch of articles to the Query service
                    try {
                        HttpResponse<String> responseQuery = Unirest.post("http://localhost:8080/query/results/").header("Content-Type", "application/json").body(articles.toString()).asString();
                        if (responseQuery.getStatus() == 200) {
                            System.out.println("Batch of articles sent to Query Service successfully.");
                            articles.clear(); // Clear the list after sending
                        } else {
                            // If it fails, the array is not cleared and the next iteration will try to send the same set of articles plus a new one again
                            System.out.println("Failed to send batch of articles to Service Service. Status: " + responseQuery.getStatus());
                        }
                    }
                    catch (UnirestException e) {
                        System.out.println("Error sending articles to Query Service: " + e.getMessage());
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println("Error retrieving articles from collection " + collectionName + ": " + e.getMessage());
            e.printStackTrace();
        }
        // Send the remaining articles if any
        if (!articles.isEmpty()) {
            try {
                HttpResponse<String> responseQuery = Unirest.post("http://localhost:8080/query/results/").header("Content-Type", "application/json").body(articles.toString()).asString();
                if (responseQuery.getStatus() == 200) {
                    System.out.println("Batch of articles sent to Query Service successfully.");
                    articles.clear(); // Clear the list after sending
                } else {
                    // TODO: If it fails, I should try again to send the same set of articles using a while loop + a sleep
                    System.out.println("Failed to send batch of articles to Service Service. Status: " + responseQuery.getStatus());
                }
            }
            catch (UnirestException e) {
                System.out.println("Error sending articles to Query Service: " + e.getMessage());
            }
        }
        // Close connection after retrieving articles
        closeConnection();
        System.out.println("All articles retrieved successfully from collection " + collectionName + ".");
    }
}
