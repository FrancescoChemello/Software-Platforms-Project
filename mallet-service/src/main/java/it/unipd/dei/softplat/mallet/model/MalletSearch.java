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
     */
    public void setQuery(String query) {
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
     */
    public void setCorpus(String corpus) {
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
     */
    public void setNumTopics(Integer numTopics) {
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
     */
    public void setNumTopWordsPerTopic(Integer numTopWordsPerTopic) {
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
