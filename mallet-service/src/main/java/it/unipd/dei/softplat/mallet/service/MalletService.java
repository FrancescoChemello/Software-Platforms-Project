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
    @Value("${mallet.topics}")
    private int topicsCount;

    /**
     * Constructor for MalletService.   
     * @param httpClientService
     */
    public MalletService(HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
        this.articles = new ArrayList<>();
    }

    /**
     * Accumulate articles from the stream.
     * This method is called when new articles are received from the DataManager Service.
     * @param articles
     * @param collectionName
     * @param endOfStream
     */
    public void accumulate(List<MalletArticle> articles, String collectionName, boolean endOfStream) {
        // Accumulate articles into the internal list
        for (MalletArticle article : articles) {
            if (article != null) {
                this.articles.add(article);
            } else {
                System.out.println("Received null article in the stream for collection: " + collectionName);
            }
        }
        // Check if we have reached the end of the stream
        if (endOfStream) {
            // Process the accumulated articles
            System.out.println("Processing remaining articles for collection: " + collectionName);
            processArticles(collectionName);
        } else {
            if (this.articles.size() >= batchSize) {
                // TODO: Implement a mechanism to process only a size of batchSize articles
                // Process the articles in batches of batchSize
                System.out.println("Processing batch of articles for collection: " + collectionName);
                processArticles(collectionName);
            }
        }
    }

    /**
     * Perform the topic modeling on the articles.
     * This method processes the articles and applies the necessary transformations.
     * @param collectionName
     */
    private void processArticles(String collectionName) {
        // Get the file stopwords from resources folder
        // the stoplist file is from https://github.com/mimno/Mallet/blob/master/stoplists/en.txt
        InputStream stoplistInputStream = MalletApp.class.getResourceAsStream("/stopwords_en.txt");
        if (stoplistInputStream == null) {
            throw new RuntimeException("Stopwords file not found in classpath!");
        }

        System.out.println("Stopwords file loaded successfully for collection: " + collectionName);
        
        // Create a Pipeline for processing the articles
        ArrayList<Pipe> pipeList = new ArrayList<>();
        
        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}+")) );
        pipeList.add( new TokenSequenceRemoveStopwords(stoplistInputStream, "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );

        System.out.println("Pipes created successfully for collection: " + collectionName);

        InstanceList instances = new InstanceList (new SerialPipes(pipeList));

        // Add the articles to the InstanceList
        for (MalletArticle article : this.articles) {
            Instance instance = new Instance(article.getBodyText(), article.getLabel(), article.getId(), "");
            instances.addThruPipe(instance);
        }
        System.out.println(String.format("Number of instances (docs): %s", instances.size()));

        System.out.println("Starting topic modeling for collection: " + collectionName);

        // Prepare the topic model
        ParallelTopicModel topicModel = new ParallelTopicModel(topicsCount);
        topicModel.addInstances(instances);
        topicModel.setNumThreads(2);
        topicModel.setNumIterations(100);
        topicModel.setTopicDisplay(100, 25); // Display 25 top words for each topic
        try {
            topicModel.estimate();
        }
        catch (IOException e) {
            System.err.println("Error during topic model estimation: " + e.getMessage());
            return; // Exit if there is an error, no data loss
        }
        System.out.println("Topic model estimation completed for collection: " + collectionName);
        
        // Prepare the articles to be sent to the DataManager Service
        ArrayList<JSONObject> topicArticles = new ArrayList<JSONObject>();
        for (MalletArticle article : this.articles) {
            JSONObject dmArticle = new JSONObject();
            dmArticle.put("id", article.getId());
            dmArticle.put("issueQuery", article.getIssueQuery());
            dmArticle.put("label", article.getLabel());
            dmArticle.put("type", article.getType());
            // Extract the top words for each topic
            List<String> topics = new ArrayList<>();
            for (int t = 0; t < topicsCount; t++) {
                for (Object obj : topicModel.getTopWords(topicsCount)[t]) {
                    topics.add((String) obj);
                }
            }
            dmArticle.put("topics", new JSONArray(topics));
            dmArticle.put("sectionId", article.getSectionId());
            dmArticle.put("sectionName", article.getSectionName());
            dmArticle.put("webPublicationDate", article.getWebPublicationDate());
            dmArticle.put("webTitle", article.getWebTitle());
            dmArticle.put("webUrl", article.getWebUrl());
            dmArticle.put("bodyText", article.getBodyText());

            topicArticles.add(dmArticle);
        }

        // Send the articles to the DataManager Service
        ResponseEntity<String> responseDataManager = httpClientService.postRequest("http://localhost:8080/topics/", topicArticles.toString());
        if (responseDataManager.getStatusCode() == HttpStatus.OK) {
            System.out.println("Successfully sent articles to DataManager Service for collection: " + collectionName);
            // Clear the articles list after processing
            this.articles.clear();
        } else {
            System.out.println("Failed to send articles to DataManager Service for collection: " + collectionName + ". Status code: " + responseDataManager.getStatusCode());
        }
        // If an error occurs during the REST call, the articles will not be cleared, allowng for retrying
        if (!this.articles.isEmpty()) {
            responseDataManager = httpClientService.postRequest("http://localhost:8080/topics/", topicArticles.toString());
            if (responseDataManager.getStatusCode() == HttpStatus.OK) {
                System.out.println("Successfully sent articles to DataManager Service for collection: " + collectionName);
                // Clear the articles list after processing
                this.articles.clear();
            } else {
                System.out.println("Failed to send articles to DataManager Service for collection: " + collectionName + ". Status code: " + responseDataManager.getStatusCode());
                // TODO: If it fails, I should try again to send the same set of articles using a while loop + a sleep
            }
        }
    }
}
