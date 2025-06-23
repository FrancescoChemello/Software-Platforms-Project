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
    private String issueString;
    @NotNull @NotEmpty
    private String label;
    @NotNull @NotEmpty
    private String type;
    @NotNull
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
     * @param issueString
     * @param label
     * @param type
     * @param webPublicationDate
     */
    public ElasticArticle(String id, String issueString, String label, String type, Date webPublicationDate) {
        this.id = id;
        this.issueString = issueString;
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
     * @throws IllegalArgumentException if the ID is null or empty
     */
    public void setId(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        this.id = id;
    }

    /**
     * Returns the issue query associated with the article.
     * @return the issue query
     * @see it.unipd.dei.softplat.monitoring.model.MonitoringRequest
     */
    public String getissueString() {
        return this.issueString;
    }

    /**
     * Sets the issue query associated with the article.
     * @param issueString
     * @see it.unipd.dei.softplat.monitoring.model.MonitoringRequest
     * @throws IllegalArgumentException if the issue string is null or empty
     */
    public void setissueString(String issueString) {
        if (issueString == null || issueString.isEmpty()) {
            throw new IllegalArgumentException("Issue string cannot be null or empty");
        }
        this.issueString = issueString;
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
     * @throws IllegalArgumentException if the label is null or empty
     */
    public void setLabel(String label) {
        if (label == null || label.isEmpty()) {
            throw new IllegalArgumentException("Label cannot be null or empty");
        }
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
     * @throws IllegalArgumentException if the type is null or empty
     */
    public void setType(String type) {
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("Type cannot be null or empty");
        }
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
     * @throws IllegalArgumentException if the web publication date is null
     */
    public void setWebPublicationDate(Date webPublicationDate) {
        if (webPublicationDate == null) {
            throw new IllegalArgumentException("Web publication date cannot be null");
        }
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
     * @throws IllegalArgumentException if the web title is null or empty
     */
    public void setWebTitle(String webTitle) {
        if (webTitle == null || webTitle.isEmpty()) {
            throw new IllegalArgumentException("Web title cannot be null or empty");
        }
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
     * @throws IllegalArgumentException if the body text is null or empty
     */
    public void setBodyText(String bodyText) {
        if (bodyText == null || bodyText.isEmpty()) {
            throw new IllegalArgumentException("Body text cannot be null or empty");
        }
        this.bodyText = bodyText;
    }

}
