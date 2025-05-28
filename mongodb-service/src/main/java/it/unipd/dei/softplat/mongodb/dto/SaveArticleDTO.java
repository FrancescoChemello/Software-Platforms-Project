/**
 * SearchArticleDTO.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.mongodb.dto;

import java.util.List;

import it.unipd.dei.softplat.mongodb.model.MongoArticle;

public class SaveArticleDTO {
    
    private List<MongoArticle> articles;
    private String collectionName;

    /**
     * Default constructor for SearchArticleDTO.
     * This constructor is required for frameworks that require a no-argument constructor,
     */
    public SaveArticleDTO() { }
    
    /**
     * Constructor for SearchArticleDTO.
     * This constructor initializes a SearchArticleDTO object with the provided parameters.
     * @param collectionName
     * @param ids
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
