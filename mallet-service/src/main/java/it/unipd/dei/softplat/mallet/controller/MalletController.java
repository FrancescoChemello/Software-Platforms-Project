/**
 * MalletController.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.mallet.controller;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import it.unipd.dei.softplat.mallet.model.MalletArticle;
import it.unipd.dei.softplat.mallet.model.MalletSearch;
import it.unipd.dei.softplat.mallet.service.MalletService;
import jakarta.validation.Valid;
import it.unipd.dei.softplat.mallet.dto.AccumulateMalletArticlesDTO;

@RestController
public class MalletController {
    
    private final MalletService malletService;

    // For logging
    private static final Logger logger = LogManager.getLogger(MalletController.class);

    /**
     * Default constructor for MalletController.
     * @param malletService The service to handle Mallet operations.
     */
    @Autowired
    public MalletController(MalletService malletService) {
        this.malletService = malletService;
    }

    /**
     * Endpoint to search articles
     * This endpoint receives a MalletSearch object containing the search parameters
     * and starts the search process.
     * @param queryString
     * @return
     */
    @PostMapping("/mallet/search/")
    public ResponseEntity<?> search(@Valid @RequestBody MalletSearch queryString) {
        String query = queryString.getQuery();
        String corpus = queryString.getCorpus();
        Integer numTopics = queryString.getNumTopics();
        Integer numTopWordsPerTopic = queryString.getNumTopWordsPerTopic();
        Date startDate = queryString.getStartDate();
        Date endDate = queryString.getEndDate();
        // Validate the input
        if (query == null || query.isEmpty()) {
            logger.error("Query is required for search.");
            return ResponseEntity.badRequest().body("Query is required for search.");
        }
        if (corpus == null || corpus.isEmpty()) {
            logger.error("Corpus is required for search.");
            return ResponseEntity.badRequest().body("Corpus is required for search.");
        }
        if (numTopics == null || numTopics <= 0) {
            logger.error("Number of topics must be a positive integer.");
            return ResponseEntity.badRequest().body("Number of topics must be a positive integer.");
        }
        if (numTopWordsPerTopic == null || numTopWordsPerTopic <= 0) {
            logger.error("Number of top words per topic must be a positive integer.");
            return ResponseEntity.badRequest().body("Number of top words per topic must be a positive integer.");
        }
        if (endDate != null && startDate != null && endDate.before(startDate)) {
            logger.error("End date cannot be before start date.");
            return ResponseEntity.badRequest().body("End date cannot be before start date.");
        }

        // Start the service
        malletService.search(query, corpus, numTopics, numTopWordsPerTopic, startDate, endDate);
        
        logger.info("Search completed successfully for query: " + query);
        
        return ResponseEntity.ok().body("Search completed successfully for query: " + query);
    }

    /**
     * Endpoint to accumulate articles for Mallet processing.
     * This endpoint receives a list of MalletArticle objects and starts the accumulation process.
     * @param articles
     * @return ResponseEntity indicating the result of the operation.
     */
    @PostMapping("/mallet/accumulate/")
    public ResponseEntity<?> accumulate(@Valid @RequestBody AccumulateMalletArticlesDTO articles) {
        List<MalletArticle> articlesList = articles.getArticles();
        String collectionName = articles.getCollectionName();
        String query = articles.getQuery();
        boolean endOfStream = articles.isEndOfStream();
        // Validate the input
        if (articlesList == null) {
            logger.error("No articles received for accumulation.");
            return ResponseEntity.badRequest().body("No articles received for accumulation.");
        }
        if (collectionName == null || collectionName.isEmpty()) {
            logger.error("Collection name is required for accumulation.");
            return ResponseEntity.badRequest().body("Collection name is required for accumulation.");
        }
        if (query == null || query.isEmpty()) {
            logger.error("Collection name is required for accumulation.");
            return ResponseEntity.badRequest().body("Collection name is required for accumulation.");
        }
        // Start the service
        malletService.accumulate(articlesList, collectionName, query, endOfStream);
        
        logger.info("Mallet accumulation started for collection: " + collectionName);
        
        return ResponseEntity.ok().body("Mallet accumulation for collection " + collectionName + " started successfully.");
    }
}
