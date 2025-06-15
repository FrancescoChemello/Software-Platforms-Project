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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            System.out.println("Error creating index: " + e.getMessage());
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
                System.out.println("Bulk indexing had errors.");
                // Iterate through the items in the response to check for errors
                System.out.println("Errors occurred while indexing articles in collection: " + collectionName);
                for (BulkResponseItem item : opResult.items()) {
                    var error = item.error();
                    if (error != null) {
                        System.out.println("Error indexing article with ID " + item.id() + ": " + error.reason());
                    } 
                }
            } else {
                System.out.println("Articles indexed successfully in collection: " + collectionName);
            }
        } 
        catch (IOException e) {
            System.out.println("Error indexing articles: " + e.getMessage());
        }

        // Refresh the index to make the indexed articles available for search
        try {
            esClient.indices().refresh(r -> r.index(collectionName));
        } catch (IOException e) {
            System.out.println("Error refreshing index: " + e.getMessage());
        }
    }

    public void getArticlesByQuery(String query, String corpus, Date startDate, Date endDate) {
        // TODO: Implement the query logic based on the requirements
        ArrayList<String> documentsID = new ArrayList<>();
        
        // To convert Date to ISO 8601 format
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Search for articles in the specified collection using a match phrase query
        try {
            SearchResponse<ElasticArticle> response = esClient.search(s -> s
                .index(corpus)
                .query(q -> q
                    .bool(b -> {
                        b.must(m -> m
                            .multiMatch(mm -> mm
                                .fields("webTitle", "bodyText")
                                .query(query)
                            )
                        );
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
                System.out.println("Found " + totalHits.value() + " articles matching the query: " + query);
                for (Hit<ElasticArticle> hit : response.hits().hits()) {
                    ElasticArticle article = hit.source();
                    if (article != null) {
                        documentsID.add(article.getId());
                    } else {
                        System.out.println("Received null article in the response.");
                    }
                }
                // Send the list of article IDs to MongoDB service to get the full articles
                if (!documentsID.isEmpty()) {
                    JSONObject articleIDs = new JSONObject();
                    articleIDs.put("collectionName", corpus);
                    articleIDs.put("ids", new JSONArray(documentsID));
                    ResponseEntity<?> mongoRequest = httpClientService.postRequest("http://localhost:8080/mongodb/get-articles/", articleIDs.toString());
                    if (mongoRequest != null && mongoRequest.getStatusCode() == HttpStatus.OK) {
                        System.out.println("Articles to retreive sent successfully to MongoDB service.");
                    } else {
                        int attempts = 0;
                        while (attempts < 5) {
                            // Retry the request to MongoDB service
                            mongoRequest = httpClientService.postRequest("http://localhost:8080/mongodb/get-articles/", articleIDs.toString());
                            if (mongoRequest != null && mongoRequest.getStatusCode() == HttpStatus.OK) {
                                System.out.println("Articles to retreive sent successfully to MongoDB service.");
                                break;
                            } else {
                                attempts++;
                                // Sleep for a while before retrying
                                try {
                                    Thread.sleep(2000 * attempts); // Sleep for 2 * attempts seconds before retrying
                                } catch (InterruptedException e) {
                                    System.out.println("Retry interrupted: " + e.getMessage());
                                    Thread.currentThread().interrupt(); // Restore the interrupted status
                                }
                                System.out.println("Failed to send articles to retreive to MongoDB service. Status: " + (mongoRequest != null ? mongoRequest.getStatusCode() : "No response received"));
                            }
                        }
                    }
                }
            } else {
                System.out.println("No articles found matching the query: " + query);
            }
        } 
        catch (IOException e) {
            System.out.println("Error retrieving articles: " + e.getMessage());
        }
    }
}
