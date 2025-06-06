/**
 * ArticleTopics.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.datamanager.model;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class ArticleTopics {
    
    @NotNull @NotEmpty
    private String id;
    @NotNull @NotEmpty
    private String issueQuery;
    @NotNull @NotEmpty
    private String label;
    @NotNull @NotEmpty
    private String type;
    @NotNull @NotEmpty
    private List<String> topics;
    @NotNull @NotEmpty
    private String sectionId;
    @NotNull @NotEmpty
    private String sectionName;
    @NotNull @NotEmpty
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z", message = "Web publication date must be in the format YYYY-MM-DDTHH:MM:SSZ")
    private String webPublicationDate;
    @NotNull @NotEmpty
    private String webTitle;
    @NotNull @NotEmpty
    @Pattern(regexp = "https?://[\\w.-]+(?:/[\\w.-]*)*", message = "Web URL must be a valid URL")
    private String webUrl;
    @NotNull @NotEmpty
    private String bodyText;

    /**
     * Default constructor for Article.
     * This constructor is required for frameworks that require a no-argument constructor,
     */
    public ArticleTopics() { }
    
    /**
     * Constructor for Article.
     * This constructor initializes an Article object with the provided parameters.
     * @param id
     * @param issueQuery
     * @param label
     * @param type
     * @param sectionId
     * @param sectionName
     * @param webPublicationDate
     * @param webTitle
     * @param webUrl
     * @param bodyText
     */
    public ArticleTopics(String id, String issueQuery, String label, String type, List<String> topics, String sectionId, String sectionName, String webPublicationDate, String webTitle, String webUrl, String bodyText) {
        this.id = id;
        this.issueQuery = issueQuery;
        this.label = label;
        this.type = type;
        this.topics = topics;
        this.sectionId = sectionId;
        this.sectionName = sectionName;
        this.webPublicationDate = webPublicationDate;
        this.webTitle = webTitle;
        this.webUrl = webUrl;
        this.bodyText = bodyText;
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
     * Returns the list of topics associated with the article.
     * @return the list of topics
     */
    public List<String> getTopics() {
        return this.topics;
    }

    /**
     * Sets the list of topics associated with the article.
     * @param topics
     */
    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    /**
     * Returns the section ID of the article.
     * @return the section ID
     */
    public String getSectionId() {
        return this.sectionId;
    }

    /**
     * Sets the section ID of the article.
     * @param sectionId
     */
    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    /**
     * Returns the section name of the article.
     * @return the section name
     */
    public String getSectionName() {
        return this.sectionName;
    }

    /**
     * Sets the section name of the article.
     * @param sectionName
     */
    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    /**
     * Returns the web publication date of the article.
     * @return the web publication date in format YYYY-MM-DDTHH:MM:SSZ
     */
    public String getWebPublicationDate() {
        return this.webPublicationDate;
    }

    /**
     * Sets the web publication date of the article.
     * @param webPublicationDate
     */
    public void setWebPublicationDate(String webPublicationDate) {
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
     * Returns the web URL of the article.
     * @return the web URL
     */
    public String getWebUrl() {
        return this.webUrl;
    }

    /**
     * Sets the web URL of the article.
     * @param webUrl
     */
    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
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
