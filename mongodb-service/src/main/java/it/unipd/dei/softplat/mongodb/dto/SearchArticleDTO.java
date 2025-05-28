/**
 * SearchArticleDTO.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.mongodb.dto;

import java.util.List;

public class SearchArticleDTO {

    private String collectionName;
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
     * @param ids
     */
    public SearchArticleDTO(String collectionName, List<String> ids) {
        this.collectionName = collectionName;
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
     */
    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
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
     */
    public void setIds(List<String> ids) {
        this.ids = ids;
    }

}
