/**
 * SearchArticleDTO.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.elasticsearch.dto;

import java.util.Date;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class SearchArticleDTO {
    
    @NotNull @NotEmpty
    private String query;
    @NotNull @NotEmpty
    private String corpus;
    private Date startDate;
    private Date endDate;

    /**
     * Defautl constructor for SearchArticleDTO.
     * This constructor is required for frameworks that require a no-argument constructor.
     */
    public SearchArticleDTO() { }

    /**
     * Constructor for SearchArticleDTO.
     * @param query The search query.
     * @param corpus The corpus to search in.
     * @param startDate The start date for the search.
     * @param endDate The end date for the search.
     */
    public SearchArticleDTO(String query, String corpus, Date startDate, Date endDate) {
        this.query = query;
        this.corpus = corpus;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Gets the search query.
     * @return The search query.
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the search query.
     * @param query The search query to set.
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Gets the corpus to search in.
     * @return The corpus.
     */
    public String getCorpus() {
        return corpus;
    }

    /**
     * Sets the corpus to search in.
     * @param corpus The corpus to set.
     */
    public void setCorpus(String corpus) {
        this.corpus = corpus;
    }

    /**
     * Gets the start date for the search.
     * @return The start date.
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date for the search.
     * @param startDate The start date to set.
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Gets the end date for the search.
     * @return The end date.
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date for the search.
     * @param endDate The end date to set.
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}

