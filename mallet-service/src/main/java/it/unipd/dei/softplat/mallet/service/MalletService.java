/**
 * MalletService.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.mallet.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import it.unipd.dei.softplat.http.service.HttpClientService;
import it.unipd.dei.softplat.mallet.MalletApp;
import it.unipd.dei.softplat.mallet.model.MalletArticle;

@Service
public class MalletService {

    private ArrayList<MalletArticle> articles;
    private final HttpClientService httpClientService;
    @Value("${mallet.batch.size}")
    private int batchSize;
    @Value("${mallet.num.iteration}")
    private int numIterations;
    @Value("${mallet.num.threads}")
    private int numThreads;
    private int numTopics;
    private int numTopWordsPerTopic;

    /**
     * Constructor for MalletService.   
     * @param httpClientService
     */
    public MalletService(HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
        this.articles = new ArrayList<>();
    }

    /**
     * Send a search request to the Elasticsearch Service.
     * @param query
     * @param corpus
     * @param numTopics
     * @param numTopWordsPerTopic
     * @param startDate
     * @param endDate
     */
    public void search(String query, String corpus, Integer numTopics, Integer numTopWordsPerTopic, Date startDate, Date endDate) {
        // Set values for numTopics and numTopWordsPerTopic
        this.numTopics = numTopics;
        this.numTopWordsPerTopic = numTopWordsPerTopic;

        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

        // Request to Elasticsearch Service to retrieve documents
        JSONObject searchRequest = new JSONObject();
        searchRequest.put("query", query);
        searchRequest.put("corpus", corpus);
        searchRequest.put("startDate", formatter.format(startDate.toInstant()));
        searchRequest.put("endDate", formatter.format(endDate.toInstant()));
        
        // Send the search request to the Elasticsearch Service
        ResponseEntity<String> response = httpClientService.postRequest("http://elasticsearch-service:8083/elastic/search/", searchRequest.toString());
        if (response != null && response.getStatusCode() == HttpStatus.OK) {
            System.out.println("Search request sent successfully to Elasticsearch Service.");
        } else {
            int attempts = 0;
            while (attempts < 5) {
                // Retry sending the request
                response = httpClientService.postRequest("http://elasticsearch-service:8083/elastic/search/", searchRequest.toString());
                if (response != null && response.getStatusCode() == HttpStatus.OK) {
                    System.out.println("Search request sent successfully to Elasticsearch Service after " + (attempts + 1) + " attempts.");
                    break; // Exit the loop if the request was successful
                } else {
                    attempts++;
                    try {
                        Thread.sleep(2000 * attempts); // Sleep for 2 * attempts seconds before retrying
                    } catch (InterruptedException e) {
                        System.out.println("Retry interrupted: " + e.getMessage());
                        Thread.currentThread().interrupt(); // Restore the interrupted status
                    }
                    System.out.println("Failed to send search request to Elasticsearch Service. Status code: " + (response != null ? response.getStatusCode() : "No response received"));
                }
            }
        }
    }

    /**
     * Accumulate retrieved articles from the stream.
     * This method is called when new articles are received from the Elasticsearch Service.
     * Articels comes from a query to the Elasticsearch Service.
     * @param articles
     * @param collectionName
     * @param query
     * @param endOfStream
     */
    public void accumulate(List<MalletArticle> articles, String collectionName, String query, boolean endOfStream) {
        // Accumulate articles into the internal list
        for (MalletArticle article : articles) {
            if (article != null) {
                this.articles.add(article);
            } else {
                System.out.println("Received null article in the stream for query " + query + " in corpus " + collectionName);
            }
        }
        // Check if we have reached the end of the stream
        if (endOfStream) {
            // Process the accumulated articles
            System.out.println("Processing remaining articles for query " + query + " in corpus " + collectionName);
            processArticles(collectionName, query);
        } else {
            if (this.articles.size() >= batchSize) {
                // Process the articles in batches of batchSize
                System.out.println("Processing batch of articles for query " + query + " in corpus " + collectionName);
                processArticles(collectionName, query);
            }
        }
    }

    /**
     * Perform the topic modeling on the articles.
     * This method processes the articles and applies the necessary transformations.
     * @param collectionName
     * @param query
     */
    private void processArticles(String collectionName, String query) {
        // Get the file stopwords from resources folder
        // the stoplist file is from https://github.com/mimno/Mallet/blob/master/stoplists/en.txt
        InputStream stoplistInputStream = MalletApp.class.getResourceAsStream("/stopwords_en.txt");
        if (stoplistInputStream == null) {
            throw new RuntimeException("Stopwords file not found in classpath!");
        }

        System.out.println("Stopwords file loaded successfully for query: " + query);
        
        // Create a Pipeline for processing the articles
        ArrayList<Pipe> pipeList = new ArrayList<>();
        
        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
        pipeList.add( new TokenSequenceRemoveStopwords(stoplistInputStream, "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );

        System.out.println("Pipes created successfully for query " + query + " in corpus " + collectionName);

        InstanceList instances = new InstanceList (new SerialPipes(pipeList));

        // Add the articles to the InstanceList
        for (MalletArticle article : this.articles) {
            Instance instance = new Instance(article.getBodyText(), article.getLabel(), article.getId(), "");
            instances.addThruPipe(instance);
        }
        System.out.println(String.format("Number of instances (docs): %s", instances.size()));
        
        System.out.println("Starting topic modeling for query " + query + " in corpus " + collectionName);
        
        // Prepare the topic model
        ParallelTopicModel topicModel = new ParallelTopicModel(numTopics);
        topicModel.addInstances(instances);
        topicModel.setNumThreads(numThreads);
        topicModel.setNumIterations(numIterations);
        topicModel.setTopicDisplay(100, numTopWordsPerTopic);
        try {
            topicModel.estimate();
        }
        catch (IOException e) {
            System.err.println("Error during topic model estimation: " + e.getMessage());
            return; // Exit if there is an error, no data loss
        }
        System.out.println("Topic model estimation completed for query " + query + " in corpus " + collectionName);
        
        /**
         * Example of the response sent to the Client Service
         * 
         * {
         * "query": "ChatGPT",
         * "topics": [
         *      {
         *          "id": 0,
         *         "topWords": [
         *                "word1", "word2", "word3"
         *           ]
         *      }, 
         *   ...
         *   ]
         * }
         */

        // Prepare the articles to be sent to the Client Service
        JSONObject queryResult = new JSONObject();
        queryResult.put("query", query);
        ArrayList<JSONObject> articleTopics = new ArrayList<JSONObject>();
        // Extract the top words from each article
        for (MalletArticle article : this.articles) {
            JSONObject topwordsArticle = new JSONObject();
            // Extract the top words for each topic
            List<String> topics = new ArrayList<>();
            for (int t = 0; t < numTopics; t++) {
                for (Object obj : topicModel.getTopWords(numTopics)[t]) {
                    topics.add((String) obj);
                }
            }
            // Sort topics in alphabetical order
            topics.sort(String::compareTo);
            topwordsArticle.put("id", article.getId());
            topwordsArticle.put("topWords", new JSONArray(topics));
            // Add the article with its topics to the list
            articleTopics.add(topwordsArticle);
        }
        // Add the articles to the query result
        queryResult.put("topics", new JSONArray(articleTopics));

        // For debugging purposes, print the query result
        System.out.println("Query Result: " + queryResult.toString(2));
        
        // Send the articles to the CLient Service
        ResponseEntity<String> responseClientService = httpClientService.postRequest("http://client-service:8080/client/query-result/", queryResult.toString());
        if (responseClientService != null && responseClientService.getStatusCode() == HttpStatus.OK) {
            System.out.println("Successfully sent query result to Client Service.");
            // Clear the articles list after processing
            this.articles.clear();
        } else {
            System.out.println("Failed to send query result to Client Service. Status code: " + (responseClientService != null ? responseClientService.getStatusCode() : "No response received"));
            int attempts = 0;
            while(attempts < 5) {
                // Retry sending the request
                responseClientService = httpClientService.postRequest("http://client-service:8080/client/query-result/", queryResult.toString());
                if (responseClientService != null && responseClientService.getStatusCode() == HttpStatus.OK) {
                    System.out.println("Successfully sent query result to Client Service after " + (attempts + 1) + " attempts.");
                    this.articles.clear(); // Clear the articles list after processing
                    break; // Exit the loop if the request was successful
                } else {
                    attempts++;
                    try {
                        Thread.sleep(2000 * attempts); // Sleep for 2 * attempts seconds before retrying
                    } catch (InterruptedException e) {
                        System.out.println("Retry interrupted: " + e.getMessage());
                        Thread.currentThread().interrupt(); // Restore the interrupted status
                    }
                    System.out.println("Failed to send query result to Client Service. Status code: " + (responseClientService != null ? responseClientService.getStatusCode() : "No response received"));
                }
            }
        }
    }
}
