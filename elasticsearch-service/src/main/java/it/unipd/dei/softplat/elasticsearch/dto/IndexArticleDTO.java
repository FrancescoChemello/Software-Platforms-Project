/**
 * IndexArticleDTO.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

 package it.unipd.dei.softplat.elasticsearch.dto;

import java.util.List;

import it.unipd.dei.softplat.elasticsearch.model.ElasticArticle;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

public class IndexArticleDTO {
    
    @NotNull @NotEmpty
    private List<ElasticArticle> articles;
    @NotNull @NotEmpty
    private String collectionName;

    /**
     * Default constructor for IndexArticleDTO.
     * This constructor is required for frameworks that require a no-argument constructor.
     */
    public IndexArticleDTO() { }

    /**
     * Constructor for IndexArticleDTO.
     * This constructor initializes an IndexArticleDTO object with the provided parameters.
     * @param articles
     * @param collectionName
     */
    public IndexArticleDTO(List<ElasticArticle> articles, String collectionName) {
        this.articles = articles;
        this.collectionName = collectionName;
    }

    /**
     * Returns the list of articles to index.
     * @return the list of articles
     */
    public List<ElasticArticle> getArticles() {
        return articles;
    }

    /**
     * Sets the list of articles to index.
     * @param articles
     */
    public void setArticles(List<ElasticArticle> articles) {
        this.articles = articles;
    }

    /**
     * Returns the name of the collection in Elasticsearch where the articles should be indexed.
     * @return the collection name
     */
    public String getCollectionName() {
        return collectionName;
    }

    /**
     * Sets the name of the collection in Elasticsearch where the articles should be indexed.
     * @param collectionName
     */
    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }
}
