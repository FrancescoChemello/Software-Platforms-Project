/**
 * SearchArticleDTO.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.mongodb.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

import it.unipd.dei.softplat.mongodb.model.MongoArticle;

/**
 * This class is intended to represent a Data Transfer Object (DTO) for saving articles in MongoDB.
 * It contains a list of articles and the name of the collection where they should be saved.
 */
public class SaveArticleDTO {
    
    @NotNull @NotEmpty
    private List<MongoArticle> articles;
    @NotNull @NotEmpty
    private String collectionName;

    /**
     * Default constructor for SearchArticleDTO.
     * This constructor is required for frameworks that require a no-argument constructor,
     */
    public SaveArticleDTO() { }
    
    /**
     * Constructor for SearchArticleDTO.
     * This constructor initializes a SearchArticleDTO object with the provided parameters.
     * @param articles
     * @param collectionName
     */
    public SaveArticleDTO(List<MongoArticle> articles, String collectionName) {
        this.collectionName = collectionName;
        this.articles = articles;
    }

        /**
     * Returns the list of articles to search for.
     * @return the list of articles
     */
    public List<MongoArticle> getArticles() {
        return articles;
    }

    /**
     * Sets the list of article to search for.
     * @param articles
     */
    public void setArticles(List<MongoArticle> articles) {
        this.articles = articles;
    }

    /**
     * Returns the name of the collection in MongoDB.
     * @return the collection name
     */
    public String getCollectionName() {
        return collectionName;
    }

    /**
     * Sets the name of the collection in MongoDB.
     * @param collectionName
     */
    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }
}
