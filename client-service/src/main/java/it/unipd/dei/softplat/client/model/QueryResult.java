/**
 * QueryResult.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.client.model;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class QueryResult {
    @NotNull @NotEmpty
    private String query;
    @NotNull
    private List<QueryTopic> topics; 

    /**
     * Default constructor.
     * This constructor is used for serialization/deserialization purposes.
     */
    public QueryResult() { }

    /**
     * Constructor to create a QueryResult with a query string and a list of topics.
     * @param query
     * @param topics
     */
    public QueryResult(String query, List<QueryTopic> topics) {
        this.query = query;
        this.topics = topics;
    }

    /**
     * Gets the query string.
     * @return the query string
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the query string.
     * @param query the query string to set
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Gets the list of topics.
     * @return the list of topics
     */
    public List<QueryTopic> getTopics() {
        return topics;
    }

    /**
     * Sets the list of topics.
     * @param topics the list of topics to set
     */
    public void setTopics(List<QueryTopic> topics) {
        this.topics = topics;
    }
}
