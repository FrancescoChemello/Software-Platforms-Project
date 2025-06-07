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
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.topics.*;
import it.unipd.dei.softplat.mallet.MalletApp;
import it.unipd.dei.softplat.mallet.model.MalletArticle;
import it.unipd.dei.softplat.http.service.HttpClientService;

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

        // Request to Elasticsearch Service to retrieve documents
        JSONObject searchRequest = new JSONObject();
        searchRequest.put("query", query);
        searchRequest.put("corpus", corpus);
        searchRequest.put("startDate", startDate);
        searchRequest.put("endDate", endDate);
        
        // Send the search request to the Elasticsearch Service
        ResponseEntity<String> response = httpClientService.postRequest("http://localhost:8080/elastic/search/", searchRequest.toString());
        if (response.getStatusCode() == HttpStatus.OK) {
            System.out.println("Search request sent successfully to Elasticsearch Service.");
        } else {
            System.out.println("Failed to send search request to Elasticsearch Service. Status code: " + response.getStatusCode());
            // TODO: Implement a mechanism to send again the request after some time.
        }
    }

    /**
     * Accumulate retrieved articles from the stream.
     * This method is called when new articles are received from the Elasticsearch Service.
     * Articels comes from a query to the Elasticsearch Service.
     * @param articles
     * @param collectionName
     * @param endOfStream
     */
    public void accumulate(List<MalletArticle> articles, String query, boolean endOfStream) {
        // Accumulate articles into the internal list
        for (MalletArticle article : articles) {
            if (article != null) {
                this.articles.add(article);
            } else {
                System.out.println("Received null article in the stream for query: " + query);
            }
        }
        // Check if we have reached the end of the stream
        if (endOfStream) {
            // Process the accumulated articles
            System.out.println("Processing remaining articles for query: " + query);
            processArticles(query);
        } else {
            if (this.articles.size() >= batchSize) {
                // TODO: Implement a mechanism to process only a size of batchSize articles
                // Process the articles in batches of batchSize
                System.out.println("Processing batch of articles for query: " + query);
                processArticles(query);
            }
        }
    }

    /**
     * Perform the topic modeling on the articles.
     * This method processes the articles and applies the necessary transformations.
     * @param query
     */
    private void processArticles(String query) {

        // TODO: check the values for numTopic and numTopWordsPerTopic
        // TODO: modify the response sent back to the Client service.

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

        System.out.println("Pipes created successfully for query: " + query);

        InstanceList instances = new InstanceList (new SerialPipes(pipeList));

        // Add the articles to the InstanceList
        for (MalletArticle article : this.articles) {
            Instance instance = new Instance(article.getBodyText(), article.getLabel(), article.getId(), "");
            instances.addThruPipe(instance);
        }
        System.out.println(String.format("Number of instances (docs): %s", instances.size()));

        System.out.println("Starting topic modeling for query: " + query);

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
        System.out.println("Topic model estimation completed for query: " + query);
        
        // Prepare the articles to be sent to the Client Service
        ArrayList<JSONObject> queryResult = new ArrayList<JSONObject>();
        for (MalletArticle article : this.articles) {
            JSONObject queryArticle = new JSONObject();
            queryArticle.put("query", query);
            // Extract the top words for each topic
            List<String> topics = new ArrayList<>();
            for (int t = 0; t < numTopics; t++) {
                for (Object obj : topicModel.getTopWords(numTopics)[t]) {
                    topics.add((String) obj);
                }
            }
            queryArticle.put("topics", new JSONArray(topics));
            queryArticle.put("id", article.getId());
            queryArticle.put("issueQuery", article.getIssueQuery());
            queryArticle.put("label", article.getLabel());
            queryArticle.put("type", article.getType());
            queryArticle.put("sectionId", article.getSectionId());
            queryArticle.put("sectionName", article.getSectionName());
            queryArticle.put("webPublicationDate", article.getWebPublicationDate());
            queryArticle.put("webTitle", article.getWebTitle());
            queryArticle.put("webUrl", article.getWebUrl());
            queryArticle.put("bodyText", article.getBodyText());

            queryResult.add(queryArticle);
        }

        // Send the articles to the CLient Service
        ResponseEntity<String> responseClientService = httpClientService.postRequest("http://localhost:8080/client/query-result/", queryResult.toString());
        if (responseClientService.getStatusCode() == HttpStatus.OK) {
            System.out.println("Successfully sent query result to Client Service.");
            // Clear the articles list after processing
            this.articles.clear();
        } else {
            System.out.println("Failed to send query result to Client Service. Status code: " + responseClientService.getStatusCode());
            // TODO: If it fails, I should try again to send the same set of articles using a while loop + a sleep
        }
    }
}
