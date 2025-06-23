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

/**
 * This class is intended to represent a Data Transfer Object (DTO) for searching articles in MongoDB.
 * It contains the name of the collection and a list of article IDs to search for.
 */
public class SearchArticleDTO {

    @NotNull @NotEmpty
    private String collectionName;
    @NotNull @NotEmpty
    private String query;
    @NotNull
    private List<String> ids;

    /**
     * Default constructor for SearchArticleDTO.
     * This constructor is required for frameworks that require a no-argument constructor,
     */
    public SearchArticleDTO() { }
    
    /**
     * Constructor for SearchArticleDTO.
     * This constructor initializes a SearchArticleDTO object with the provided parameters.
     * @param collectionName
     * @param query
     * @param ids
     */
    public SearchArticleDTO(String collectionName, String query, List<String> ids) {
        this.collectionName = collectionName;
        this.query = query;
        this.ids = ids;
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
     * @throws IllegalArgumentException if the collection name is null or empty
     */
    public void setCollectionName(String collectionName) {
        if (collectionName == null || collectionName.isEmpty()) {
            throw new IllegalArgumentException("Collection name cannot be null or empty");
        }
        this.collectionName = collectionName;
    }

    /**
     * Returns the query string to search for articles.
     * @return the query string
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the query string to search for articles.
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
     * Returns the list of article IDs to search for.
     * @return the list of article IDs
     */
    public List<String> getIds() {
        return ids;
    }

    /**
     * Sets the list of article IDs to search for.
     * @param ids
     * @throws IllegalArgumentException if the list of IDs is null
     */
    public void setIds(List<String> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("IDs cannot be null");
        }
        this.ids = ids;
    }
}
