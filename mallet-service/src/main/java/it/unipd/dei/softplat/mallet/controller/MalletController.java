/**
 * MalletController.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.mallet.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import it.unipd.dei.softplat.mallet.model.MalletArticle;
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
     * Endpoint to accumulate articles for Mallet processing.
     * This endpoint receives a list of MalletArticle objects and starts the accumulation process.
     * @param articles
     * @return ResponseEntity indicating the result of the operation.
     */
    @PostMapping("/mallet/accumulate/")
    public ResponseEntity<?> accumulate(@RequestBody AccumulateMalletArticlesDTO articles) {
        List<MalletArticle> articlesList = articles.getArticles();
        String collectionName = articles.getCollectionName();
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
        // Start the service
        malletService.accumulate(articlesList, collectionName, endOfStream);
        System.out.println("Mallet accumulation started for collection: " + collectionName);
        return ResponseEntity.ok().body("Mallet accumulation for collection " + collectionName + " started successfully.");
    }
}
