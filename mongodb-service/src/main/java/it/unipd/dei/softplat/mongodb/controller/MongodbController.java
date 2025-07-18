/**
 * MongodbController.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */


package it.unipd.dei.softplat.mongodb.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import it.unipd.dei.softplat.mongodb.model.MongoArticle;
import it.unipd.dei.softplat.mongodb.service.MongodbService;

import it.unipd.dei.softplat.mongodb.dto.SearchArticleDTO;
import it.unipd.dei.softplat.mongodb.dto.SaveArticleDTO;

/**
 * This class is intended to handle requests related to MongoDB operations.
 * It provides endpoints to save articles, drop collections, and search for articles.
 */
@RestController
public class MongodbController {
 
    private final MongodbService mongodbService;

    // For logging
    private static final Logger logger = LogManager.getLogger(MongodbController.class);
    
    /**
     * Default constructor for MongodbController.
     * @param mongodbService The service to handle MongoDB operations.
     */
    @Autowired
    public MongodbController(MongodbService mongodbService) {
        this.mongodbService = mongodbService;
    }

    /**
     * This method receives a list of articles from the Monitoring service
     * and processes them.
     * It is expected to be called by the Data Manager service.
     * @param articles
     * @param collectionName
     * @return
     */
    @PostMapping("/mongodb/save/")
    public ResponseEntity<?> saveArticles(@Valid @RequestBody SaveArticleDTO saveArticleDTO) {
        // Extract the articles and collection name from the DTO
        List<MongoArticle> articles = saveArticleDTO.getArticles();
        String collectionName = saveArticleDTO.getCollectionName();
        
        // Check if the articles and collection name are valid
        if(articles == null || articles.isEmpty()) {
            logger.error("No articles received.");
            return ResponseEntity.badRequest().body("No articles received.");
        }
        if (collectionName == null || collectionName.isEmpty()) {
            logger.error("No collection name provided.");
            return ResponseEntity.badRequest().body("No collection name provided.");
        } 
        
        // Start the service
        mongodbService.saveArticles(articles, collectionName);

        logger.info("Articles saved successfully in collection: " + collectionName);

        return ResponseEntity.ok().body("Articles saved successfully.");
    }
    
    /**
     * This method retrieves articles from a specific collection in MongoDB
     * based on a search query.
     * @param query
     * @param collectionName
     * @return
     */
    @PostMapping("/mongodb/get-articles/")
    public ResponseEntity<?> searchArticles(@Valid @RequestBody SearchArticleDTO searchArticleDTO) {
        // Extract the collection name and ids from the DTO
        String collectionName = searchArticleDTO.getCollectionName();
        String query = searchArticleDTO.getQuery();
        List<String> ids = searchArticleDTO.getIds();
        
        // Check if the collection name and ids are valid
        if (collectionName == null || collectionName.isEmpty()) {
            logger.error("No collection name provided.");
            return ResponseEntity.badRequest().body("No collection name provided.");
        }
        if (ids == null) {
            logger.error("The ids list cannot be null.");
            return ResponseEntity.badRequest().body("The ids list cannot be null.");
        }

        // Start the service
        mongodbService.getArticlesById(collectionName, query, ids);

        logger.info("Articles retrieved successfully from collection: " + collectionName);

        return ResponseEntity.ok().body("Articles retrieved successfully.");
    }
}
