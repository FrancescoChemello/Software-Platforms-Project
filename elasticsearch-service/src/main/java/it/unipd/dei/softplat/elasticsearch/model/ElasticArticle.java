/**
 * ElasticArticle.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */


package it.unipd.dei.softplat.elasticsearch.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class ElasticArticle {
    
    @NotNull @NotEmpty
    private String id;
    @NotNull @NotEmpty
    private String issueQuery;
    @NotNull @NotEmpty
    private String label;
    @NotNull @NotEmpty
    private String type;
    @NotNull @NotEmpty
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Date webPublicationDate;
    @NotNull @NotEmpty
    private String webTitle;
    @NotNull @NotEmpty
    private String bodyText;

    /**
     * Default constructor for ElasticArticle.
     * This constructor is required for frameworks that require a no-argument constructor,
     * such as Spring when deserializing JSON requests.
     */
    public ElasticArticle() { }

    /**
     * Constructor for ElasticArticle.
     * This constructor initializes an ElasticArticle object with the provided parameters.
     * @param id
     * @param issueQuery
     * @param label
     * @param type
     * @param webPublicationDate
     */
    public ElasticArticle(String id, String issueQuery, String label, String type, Date webPublicationDate) {
        this.id = id;
        this.issueQuery = issueQuery;
        this.label = label;
        this.type = type;
        this.webPublicationDate = webPublicationDate;
    }


    /**
     * Returns the unique identifier of the article.
     * @return the article ID
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets the unique identifier of the article.
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the issue query associated with the article.
     * @return the issue query
     * @see it.unipd.dei.softplat.monitoring.model.MonitoringRequest
     */
    public String getIssueQuery() {
        return this.issueQuery;
    }

    /**
     * Sets the issue query associated with the article.
     * @param issueQuery
     * @see it.unipd.dei.softplat.monitoring.model.MonitoringRequest
     */
    public void setIssueQuery(String issueQuery) {
        this.issueQuery = issueQuery;
    }

    /**
     * Returns the label of the article.
     * @return the label
     * @see it.unipd.dei.softplat.monitoring.model.MonitoringRequest
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Sets the label of the article.
     * @param label
     * @see it.unipd.dei.softplat.monitoring.model.MonitoringRequest
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Returns the type of the article.
     * @return the article type
     */
    public String getType() {
        return this.type;
    }

    /**
     * Sets the type of the article.
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * Returns the web publication date of the article.
     * @return the web publication date
     */
    public Date getWebPublicationDate() {
        return this.webPublicationDate;
    }
    
    /**
     * Sets the web publication date of the article.
     * @param webPublicationDate
     */
    public void setWebPublicationDate(Date webPublicationDate) {
        this.webPublicationDate = webPublicationDate;
    }
    
    /**
     * Returns the web title of the article.
     * @return the web title
     */
    public String getWebTitle() {
        return this.webTitle;
    }

    /**
     * Sets the web title of the article.
     * @param webTitle
     */
    public void setWebTitle(String webTitle) {
        this.webTitle = webTitle;
    }

    /**
     * Returns the body text of the article.
     * @return the body text
     */
    public String getBodyText() {
        return this.bodyText;
    }

    /**
     * Sets the body text of the article.
     * @param bodyText
     */
    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;
    }

}
