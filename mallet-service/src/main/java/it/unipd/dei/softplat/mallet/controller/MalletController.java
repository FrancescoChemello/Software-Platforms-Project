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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import it.unipd.dei.softplat.mallet.model.MalletArticle;
import it.unipd.dei.softplat.mallet.model.MalletSearch;
import it.unipd.dei.softplat.mallet.service.MalletService;
import it.unipd.dei.softplat.mallet.dto.AccumulateMalletArticlesDTO;

@RestController
public class MalletController {
    
    private final MalletService malletService;

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
    public ResponseEntity<?> search(@RequestBody MalletSearch queryString) {
        String query = queryString.getQuery();
        String corpus = queryString.getCorpus();
        Integer numTopics = queryString.getNumTopics();
        Integer numTopWordsPerTopic = queryString.getNumTopWordsPerTopic();
        Date startDate = queryString.getStartDate();
        Date endDate = queryString.getEndDate();
        // Validate the input
        if (query == null || query.isEmpty()) {
            System.out.println("Query is required for search.");
            return ResponseEntity.badRequest().body("Query is required for search.");
        }
        if (corpus == null || corpus.isEmpty()) {
            System.out.println("Corpus is required for search.");
            return ResponseEntity.badRequest().body("Corpus is required for search.");
        }
        if (numTopics == null || numTopics <= 0) {
            System.out.println("Number of topics must be a positive integer.");
            return ResponseEntity.badRequest().body("Number of topics must be a positive integer.");
        }
        if (numTopWordsPerTopic == null || numTopWordsPerTopic <= 0) {
            System.out.println("Number of top words per topic must be a positive integer.");
            return ResponseEntity.badRequest().body("Number of top words per topic must be a positive integer.");
        }
        if (startDate == null) {
            System.out.println("Start date is required for search.");
            return ResponseEntity.badRequest().body("Start date is required for search.");
        }
        // if (endDate == null) {
        //     System.out.println("End date is required for search.");
        //     return ResponseEntity.badRequest().body("End date is required for search.");
        // }
        if (endDate != null && endDate.before(startDate)) {
            System.out.println("End date cannot be before start date.");
            return ResponseEntity.badRequest().body("End date cannot be before start date.");
        }

        // Start the service
        malletService.search(query, corpus, numTopics, numTopWordsPerTopic, startDate, endDate);
        
        System.out.println("Search completed successfully for query: " + query);
        return ResponseEntity.ok().body("Search completed successfully for query: " + query);
    }

    /**
     * Endpoint to accumulate articles for Mallet processing.
     * This endpoint receives a list of MalletArticle objects and starts the accumulation process.
     * @param articles
     * @return ResponseEntity indicating the result of the operation.
     */
    @PostMapping("/mallet/accumulate/")
    public ResponseEntity<?> accumulate(@RequestBody AccumulateMalletArticlesDTO articles) {
        List<MalletArticle> articlesList = articles.getArticles();
        String collectionName = articles.getCollectionName();
        String query = articles.getQuery();
        boolean endOfStream = articles.isEndOfStream();
        // Validate the input
        if (articlesList == null) {
            System.out.println("No articles received for accumulation.");
            return ResponseEntity.badRequest().body("No articles received for accumulation.");
        }
        if (collectionName == null || collectionName.isEmpty()) {
            System.out.println("Collection name is required for accumulation.");
            return ResponseEntity.badRequest().body("Collection name is required for accumulation.");
        }
        if (query == null || query.isEmpty()) {
            System.out.println("Collection name is required for accumulation.");
            return ResponseEntity.badRequest().body("Collection name is required for accumulation.");
        }
        // Start the service
        malletService.accumulate(articlesList, collectionName, query, endOfStream);
        System.out.println("Mallet accumulation started for collection: " + collectionName);
        return ResponseEntity.ok().body("Mallet accumulation for collection " + collectionName + " started successfully.");
    }
}
