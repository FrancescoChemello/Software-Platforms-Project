/**
 * DataManagerController.java
 * This file implements the DataManagerController class
 * which is responsible for handling data management-related requests.
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.datamanager.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import it.unipd.dei.softplat.datamanager.model.Article;
import it.unipd.dei.softplat.datamanager.service.DataManagerService;
import jakarta.validation.Valid;

public class DataManagerController {
    
    private final DataManagerService dataService;

    /**
     * Default constructor for DataManagerController.
     * @param dataService The service to handle data management operations.
     */
    @Autowired
    public DataManagerController(DataManagerService dataService) {
        this.dataService = dataService;
    }

    /**
     * This method receives a list of articles from the Monitoring service
     * and processes them.
     * It is expected to be called by the Monitoring service.
     * @param articles
     * @return A ResponseEntity indicating the result of the operation.
     */
    @PostMapping("/articles/")
    public ResponseEntity<?> getArticles(@Valid @RequestBody List<Article> articles){  
        // Process the articles received from the Monitoring service
        if(articles == null || articles.isEmpty()) {
            System.out.println("No articles received.");
            return ResponseEntity.badRequest().body("No articles received.");
        }

        // Start the service
        dataService.storingArticles(articles);
        return ResponseEntity.ok().body("Articles received successfully.");
        
    }
}
