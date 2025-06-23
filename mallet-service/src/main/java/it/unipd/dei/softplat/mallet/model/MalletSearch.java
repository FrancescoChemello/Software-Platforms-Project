/**
 * MalletSearch.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.mallet.model;

import java.util.Date;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class MalletSearch {

    @NotNull @NotEmpty
    private String query;
    @NotNull @NotEmpty
    private String corpus;
    @NotNull @NotEmpty
    private Integer numTopics;
    @NotNull @NotEmpty
    private Integer numTopWordsPerTopic;
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z", message = "Start date must be in the format YYYY-MM-DDTHH:MM:SSZ")
    private Date startDate;
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z", message = "End date must be in the format YYYY-MM-DDTHH:MM:SSZ")
    private Date endDate;

    /**
     * Default constructor for MalletSearch.
     */
    public MalletSearch() { }

    /**
     * Constructor for MalletSearch with parameters.
     * @param query
     * @param corpus
     * @param subCorpus
     * @param numTopics
     * @param numTopWordsPerTopic
     * @param startDate
     * @param endDate
     */
    public MalletSearch(String query, String corpus, Integer numTopics, Integer numTopWordsPerTopic, Date startDate, Date endDate) {
        this.query = query;
        this.corpus = corpus;
        this.numTopics = numTopics;
        this.numTopWordsPerTopic = numTopWordsPerTopic;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Returns the query string for the search.
     * @return the query string
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the query string for the search.
     * @param query
     * @throws IllegalArgumentException if the query is null or empty
     */
    public void setQuery(String query) {
        if (query == null || query.isEmpty()) {
            throw new IllegalArgumentException("Query cannot be null or empty");
        }
        this.query = query;
    }

    /**
     * Returns the corpus to search in.
     * @return the corpus
     */
    public String getCorpus() {
        return corpus;
    }

    /**
     * Sets the corpus to search in.
     * @param corpus
     * @throws IllegalArgumentException if the corpus is null or empty
     */
    public void setCorpus(String corpus) {
        if (corpus == null || corpus.isEmpty()) {
            throw new IllegalArgumentException("Corpus cannot be null or empty");
        }
        this.corpus = corpus;
    }

    /**
     * Returns the number of topics to search for.
     * @return the number of topics
     */
    public Integer getNumTopics() {
        return numTopics;
    }

    /**
     * Sets the number of topics to search for.
     * @param numTopics
     * @throws IllegalArgumentException if the number of topics is null or not a positive integer
     */
    public void setNumTopics(Integer numTopics) {
        if (numTopics == null || numTopics <= 0) {
            throw new IllegalArgumentException("Number of topics must be a positive integer");
        }
        this.numTopics = numTopics;
    }

    /**
     * Returns the number of top words per topic.
     * @return the number of top words per topic
     */
    public Integer getNumTopWordsPerTopic() {
        return numTopWordsPerTopic;
    }

    /**
     * Sets the number of top words per topic.
     * @param numTopWordsPerTopic
     * @throws IllegalArgumentException if the number of top words is null or not a positive integer
     */
    public void setNumTopWordsPerTopic(Integer numTopWordsPerTopic) {
        if (numTopWordsPerTopic == null || numTopWordsPerTopic <= 0) {
            throw new IllegalArgumentException("Number of top words per topic must be a positive integer");
        }
        this.numTopWordsPerTopic = numTopWordsPerTopic;
    }

    /**
     * Returns the start date for the search.
     * @return the start date
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date for the search.
     * @param startDate
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Returns the end date for the search.
     * @return the end date
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date for the search.
     * @param endDate
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }  
}
