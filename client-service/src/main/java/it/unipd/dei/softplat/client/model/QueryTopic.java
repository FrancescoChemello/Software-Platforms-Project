/**
 * QueryTopic.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.client.model;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public class QueryTopic {
    @NotNull
    private String id;
    @NotNull
    private List<String> topWords; 
    
    /**
     * Default constructor.
     * @param id
     * @param topWords
     */
    public QueryTopic(String id, List<String> topWords) {
        this.id = id;
        this.topWords = topWords;
    }

    /**
     * Gets the topic ID.
     * @return the topic ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the topic ID.
     * @param id the topic ID to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the list of top words for the topic.
     * @return the list of top words
     */
    public List<String> getTopWords() {
        return topWords;
    }

    /**
     * Sets the list of top words for the topic.
     * @param topWords the list of top words to set
     */
    public void setTopWords(List<String> topWords) {
        this.topWords = topWords;
    }
}
