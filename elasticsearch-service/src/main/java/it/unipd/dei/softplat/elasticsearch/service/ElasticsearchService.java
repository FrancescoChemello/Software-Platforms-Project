/**
 * ElasticsearchService.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.elasticsearch.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;

import org.json.JSONArray;
import org.json.JSONObject;

import it.unipd.dei.softplat.elasticsearch.model.ElasticArticle;
import it.unipd.dei.softplat.http.service.HttpClientService;

@Service
public class ElasticsearchService {
    
    private final ElasticsearchClient esClient;
    private final HttpClientService httpClientService;

    // For logging
    private static final Logger logger = LogManager.getLogger(ElasticsearchService.class);

    /**
     * Default constructor for ElasticsearchService.
     * This constructor is required for frameworks that require a no-argument constructor,
     */
    @Autowired
    public ElasticsearchService(ElasticsearchClient esClient, HttpClientService httpClientService) { 
        // Initialize the HttpClientService
        this.httpClientService = httpClientService;

        // Set up the Elasticsearch environment
        this.esClient = esClient;
    }

    /**
     * Indexes a list of articles in Elasticsearch.
     * This method receives a list of articles and calls the service to 
     * index the articles in the specified collection.
     * @param articles
     * @param collectionName
     */
    @Async
	public void indexArticles(List<ElasticArticle> articles, String collectionName) {
        // Create the index if it does not exist
        try {
            if (!esClient.indices().exists(b -> b.index(collectionName)).value()) {
                esClient.indices().create(c -> c
                    .index(collectionName)
                    .mappings(mb -> mb
                        .properties("webPublicationDate", pb -> pb
                            .date(db -> db.format("yyyy-MM-dd'T'HH:mm:ss'Z'"))
                        )
                        .properties("webTitle", pb -> pb.text(tb -> tb))
                        .properties("bodyText", pb -> pb.text(tb -> tb))
                    )
                );
            }
        }
        catch (IOException e) {
            logger.error("Error creating index: " + e.getMessage());
            return;
        }

        // Bulk request to index multiple articles at once
        BulkRequest.Builder br = new BulkRequest.Builder();

        // Iterate through the articles and add them to the bulk request
        for (ElasticArticle article : articles) {
            // Add each article to the bulk request
            br.operations(op -> op
                    .index(idx -> idx
                            .index(collectionName)
                            .id(article.getId())
                            .document(article)
                    )
            );
        }

        // Execute the bulk request
        try {
            BulkResponse opResult = esClient.bulk(br.build());
            if (opResult.errors()) {
                logger.error("Bulk indexing had errors.");
                // Iterate through the items in the response to check for errors
                logger.error("Errors occurred while indexing articles in collection: " + collectionName);
                for (BulkResponseItem item : opResult.items()) {
                    var error = item.error();
                    if (error != null) {
                        logger.error("Error indexing article with ID " + item.id() + ": " + error.reason());
                    } 
                }
            } else {
                logger.info("Articles indexed successfully in collection: " + collectionName);
            }
        } 
        catch (IOException e) {
            logger.error("Error indexing articles: " + e.getMessage());
        }

        // Refresh the index to make the indexed articles available for search
        try {
            esClient.indices().refresh(r -> r.index(collectionName));
        } catch (IOException e) {
            logger.error("Error refreshing index: " + e.getMessage());
        }
    }

    /**
     * Retrieves articles from Elasticsearch based on a query and a corpus.
     * @param query
     * @param corpus
     * @param startDate
     * @param endDate
     */
    public void getArticlesByQuery(String query, String corpus, Date startDate, Date endDate) {
        ArrayList<String> documentsID = new ArrayList<>();
        
        // To convert Date to ISO 8601 format
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        // Search for articles in the specified collection using a match phrase query
        try {
            // Check if the corpus does not exist in the Elasticsearch index
            if (!esClient.indices().exists(b -> b.index(corpus)).value()) {
                logger.warn("Corpus " + corpus + " does not exist in Elasticsearch index.");
                // Index not found, send end of stream signal to Mallet service
                indexNotFound(corpus, query);
                return;
            }

            SearchResponse<ElasticArticle> response = esClient.search(s -> s
                .index(corpus)
                .query(q -> q
                    .bool(b -> {
                        if (query.startsWith("\"") && query.endsWith("\"")) {
                            b.must(m -> m
                            .matchPhrase(mp -> mp
                            .field("bodyText")
                            .query(query.replace("\"", ""))
                            )
                            );
                            logger.info("Using match phrase query for: " + query);
                        } else {
                            b.must(m -> m
                                .multiMatch(mm -> mm
                                    .fields("webTitle", "bodyText")
                                    .query(query)
                                )
                            );
                            logger.info("Using multi-match query for: " + query);
                        }
                        // Apply filter date only if startDate or endDate is not null
                        if(startDate != null || endDate != null) {
                            b.filter(f -> f
                                .range(r -> {
                                    r.field("webPublicationDate");
                                    // Set the range for the date filter
                                    // If startDate or endDate is null, it will not be included in the range
                                    if (startDate != null) {
                                        r.gte(JsonData.of(isoFormat.format(startDate)));
                                    }
                                    if (endDate != null) {
                                        r.lte(JsonData.of(isoFormat.format(endDate)));
                                    }
                                    return r;
                                })
                            );
                        }
                        return b;
                    })
                ),
                ElasticArticle.class
            );

            // Check if the response contains hits
            TotalHits totalHits = response.hits().total();
            if (totalHits != null && totalHits.value() > 0) {
                logger.info("Found " + totalHits.value() + " articles matching the query: " + query);
                for (Hit<ElasticArticle> hit : response.hits().hits()) {
                    ElasticArticle article = hit.source();
                    if (article != null) {
                        documentsID.add(article.getId());
                    } else {
                        logger.warn("Received null article in the response.");
                    }
                }
            }
            JSONObject articleIDs = new JSONObject();
            articleIDs.put("collectionName", corpus);
            articleIDs.put("query", query);
            articleIDs.put("ids", new JSONArray(documentsID));
            // Send the article IDs to MongoDB service
            sendIdsToMongo(articleIDs);
        } 
        catch (IOException e) {
            logger.error("Error retrieving articles: " + e.getMessage());
        }
    }

    /**
     * Sends the list of article IDs to MongoDB service to retrieve the full articles.
     * This method will send the article IDs to MongoDB service to retrieve the full articles.
     * @param articleIDs
     */
    public void sendIdsToMongo(JSONObject articleIDs) {
        
        // Send the list of article IDs to MongoDB service
        ResponseEntity<?> mongoRequest = httpClientService.postRequest("http://mongodb-service:8085/mongodb/get-articles/", articleIDs.toString());
        if (mongoRequest != null && mongoRequest.getStatusCode() == HttpStatus.OK) {
            logger.info("Articles to retreive sent successfully to MongoDB service.");
        } else {
            int attempts = 0;
            while (attempts < 5) {
                // Retry the request to MongoDB service
                mongoRequest = httpClientService.postRequest("http://mongodb-service:8085/mongodb/get-articles/", articleIDs.toString());
                if (mongoRequest != null && mongoRequest.getStatusCode() == HttpStatus.OK) {
                    logger.info("Articles to retreive sent successfully to MongoDB service after "+ (attempts + 1) + " attempts.");
                    break;
                } else {
                    attempts++;
                    // Sleep for a while before retrying
                    try {
                        Thread.sleep(2000 * attempts); // Sleep for 2 * attempts seconds before retrying
                    } catch (InterruptedException e) {
                        logger.error("Retry interrupted: " + e.getMessage());
                        Thread.currentThread().interrupt(); // Restore the interrupted status
                    }
                    logger.warn("Failed to send articles to retreive to MongoDB service. Status: " + (mongoRequest != null ? mongoRequest.getStatusCode() : "No response received"));
                }
            }
        }
    }

    /**
     * Handles the case when the index is not found.
     * This method is called when the specified index does not exist in Elasticsearch.
     * It sends an end of stream signal to the Mallet service to indicate that no articles were found for the given query and corpus.
     * @param corpus
     * @param query
     */
    public void indexNotFound(String corpus, String query) {
        // Send the end of stream signal to the Mallet service
        JSONObject endOfStreamDTO = new JSONObject();
        endOfStreamDTO.put("articles", new ArrayList<>());
        endOfStreamDTO.put("collectionName", corpus);
        endOfStreamDTO.put("query", query);
        endOfStreamDTO.put("endOfStream", true);
        // Send the end of stream signal to the Mallet service
        ResponseEntity<String> responseEndOfStream = httpClientService.postRequest("http://mallet-service:8084/mallet/accumulate/", endOfStreamDTO.toString());
        if (responseEndOfStream != null && responseEndOfStream.getStatusCode() == HttpStatus.OK) {
            logger.info("End of stream signal sent to Mallet Service successfully.");
        } else {
            int attempts = 0;
            while (attempts < 5) {
                // Retry sending the end of stream signal
                responseEndOfStream = httpClientService.postRequest("http://mallet-service:8084/mallet/accumulate/", endOfStreamDTO.toString());
                if (responseEndOfStream != null && responseEndOfStream.getStatusCode() == HttpStatus.OK) {
                    logger.info("End of stream signal sent to Mallet Service successfully after " + (attempts + 1) + " attempts.");
                    break; // Exit the loop if successful
                } else {
                    attempts++;
                    // Sleep for a while before retrying
                    try {
                        Thread.sleep(2000 * attempts); // Sleep for 2 * attempts seconds before retrying
                    } catch (InterruptedException e) {
                        logger.error("Retry interrupted: " + e.getMessage());
                        Thread.currentThread().interrupt(); // Restore the interrupted status
                    }
                    logger.warn("Failed to send end of stream signal to Mallet Service. Status: " + (responseEndOfStream != null ? responseEndOfStream.getStatusCode() : "No response received"));
                }
            }
        }
    }
}
