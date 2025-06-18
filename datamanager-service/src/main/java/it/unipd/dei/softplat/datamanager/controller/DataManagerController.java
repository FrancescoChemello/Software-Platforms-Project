/**
 * DataManagerController.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.datamanager.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

import it.unipd.dei.softplat.datamanager.model.Article;
import it.unipd.dei.softplat.datamanager.service.DataManagerService;

/**
 * This class is intended to handle requests related to data management.
 */
@RestController
public class DataManagerController {
    
    private final DataManagerService dataService;

    // For logging
    private static final Logger logger = LogManager.getLogger(DataManagerController.class);

    /**
     * Default constructor for DataManagerController.
     * @param dataService The service to handle data management operations.
     */
    @Autowired
    public DataManagerController(DataManagerService dataService) {
        this.dataService = dataService;
    }

    /**
     * This method receives a list of articles with topics extracted
     * and stores them in MongoDB and Elasticsearch.
     * @param topicsArticles List of articles with topics.
     * @return ResponseEntity indicating the result of the operation.
     */
    @PostMapping("/datamanager/save-articles/")
    public ResponseEntity<?> saveArticles(@Valid @RequestBody List<Article> topicsArticles) {
        // Process the articles received from the Monitoring service
        if(topicsArticles == null || topicsArticles.isEmpty()) {
            logger.error("No articles received.");
            return ResponseEntity.badRequest().body("No articles received.");
        }

        // Start the service
        dataService.storingArticles(topicsArticles);
        logger.info("Articles with topics received successfully.");
        return ResponseEntity.ok().body("Articles with topics received successfully.");
    }
}
