/**
 * AccumulateMalletArticleDTO.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.mallet.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

import it.unipd.dei.softplat.mallet.model.MalletArticle;

public class AccumulateMalletArticlesDTO {
    
    @NotNull
    private List<MalletArticle> articles;
    @NotNull @NotEmpty
    private String collectionName;
    @NotNull @NotEmpty
    private String query;
    private boolean endOfStream;

    /**
     * Default constructor for AccumulateMalletArticleDTO.
     * This constructor is required for frameworks that require a no-argument constructor,
     */
    public AccumulateMalletArticlesDTO() { }

    /**
     * Constructor for AccumulateMalletArticleDTO.
     * This constructor initializes an AccumulateMalletArticleDTO object with the provided parameters.
     * @param articles
     * @param collectionName
     * @param query
     * @param endOfStream
     */
    public AccumulateMalletArticlesDTO(List<MalletArticle> articles, String collectionName, String query, boolean endOfStream) {
        this.articles = articles;
        this.collectionName = collectionName;
        this.query = query;
        this.endOfStream = endOfStream;
    }

    /**
     * Returns the list of articles to accumulate.
     * @return the list of articles
     */
    public List<MalletArticle> getArticles() {
        return articles;
    }

    /**
     * Sets the list of articles to accumulate.
     * @param articles
     */
    public void setArticles(List<MalletArticle> articles) {
        this.articles = articles;
    }

    /**
     * Returns the name of the collection in Mallet.
     * @return the collection name
     */
    public String getCollectionName() {
        return collectionName;
    }

    /**
     * Sets the name of the collection in Mallet.
     * @param collectionName
     */
    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    /**
     * Returns the name of the collection in Mallet.
     * @return the collection name
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the name of the collection in Mallet.
     * @param query
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Returns whether the end of the stream has been reached.
     * @return true if the end of the stream has been reached, false otherwise
     */
    public boolean isEndOfStream() {
        return endOfStream;
    }

    /**
     * Sets whether the end of the stream has been reached.
     * @param endOfStream true if the end of the stream has been reached, false otherwise
     */
    public void setEndOfStream(boolean endOfStream) {
        this.endOfStream = endOfStream;
    }
}
