/**
 * ElasticsearchController.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.elasticsearch.controller;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import it.unipd.dei.softplat.elasticsearch.service.ElasticsearchService;
import jakarta.validation.Valid;
import it.unipd.dei.softplat.elasticsearch.dto.IndexArticleDTO;
import it.unipd.dei.softplat.elasticsearch.dto.SearchArticleDTO;
import it.unipd.dei.softplat.elasticsearch.model.ElasticArticle;

/**
 * This class is intended to handle HTTP requests related to Elasticsearch operations.
 * It provides endpoints for indexing articles and retrieving articles based on a query.
 */
@RestController
public class ElasticsearchController {

    private final ElasticsearchService elasticsearchService;

    // For logging
    private static final Logger logger = LogManager.getLogger(ElasticsearchController.class);

    /**
     * Default constructor for ElasticsearchController.
     * @param elasticsearchService The service to handle Elasticsearch operations.
     */
    @Autowired
    public ElasticsearchController(ElasticsearchService elasticsearchService) {
        this.elasticsearchService = elasticsearchService;
    }
    
    /**
     * Indexes a list of articles in Elasticsearch.
     * This method receives a list of articles and a collection name, validates the input,
     * and then calls the service to index the articles in the specified collection.
     * @param indexArticles
     * @return ResponseEntity with the result of the indexing operation.
     */
    @PostMapping("/elastic/index/")
    public ResponseEntity<?> indexArticles(@Valid @RequestBody IndexArticleDTO indexArticles) {

        List<ElasticArticle> articles = indexArticles.getArticles();
        String collectionName = indexArticles.getCollectionName();
        
        // Validate the input
        if (articles == null || articles.isEmpty()) {
            logger.error("No articles received for indexing.");
            return ResponseEntity.badRequest().body("No articles received for indexing.");
        }
        if (collectionName == null || collectionName.isEmpty()) {
            logger.error("Collection name is required for indexing.");
            return ResponseEntity.badRequest().body("Collection name is required for indexing.");
        }

        // Start the service
        elasticsearchService.indexArticles(articles, collectionName);
        
        logger.info("Articles indexed successfully in collection: ", collectionName);
        
        return ResponseEntity.ok().body("Articles indexed successfully.");
    }
    
    /**
     * Retrieves articles from Elasticsearch based on a query.
     * This method receives a collection name and a query string,
     * validates the input, and then calls the service to retrieve articles matching the query.
     * @param collectionName
     * @param query
     * @return ResponseEntity with the result of the search operation.
     */
    @PostMapping("/elastic/search/")
    public ResponseEntity<?> getArticlesByQuery(@RequestBody SearchArticleDTO searchArticleDTO) {
        String query = searchArticleDTO.getQuery();
        String corpus = searchArticleDTO.getCorpus();
        Date startDate = searchArticleDTO.getStartDate();
        Date endDate = searchArticleDTO.getEndDate();
        // Validate the input

        if (query == null || query.isEmpty()) {
            logger.error("Query is required to retrieve articles.");
            return ResponseEntity.badRequest().body("Query is required to retrieve articles.");
        }
        if (corpus == null || corpus.isEmpty()) {
            logger.error("Corpus is required to retrieve articles.");
            return ResponseEntity.badRequest().body("Corpus is required to retrieve articles.");
        }
        
        // Retrieve articles by query
        elasticsearchService.getArticlesByQuery(query, corpus, startDate, endDate);
        
        logger.info("Search completed successfully for query: ", query);
        
        return ResponseEntity.ok().body("Search compleated successfully.");
    }
}
