/**
 * MalletArticle.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.mallet.model;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class MalletArticle {
    
    @NotNull @NotEmpty
    private String id;
    @NotNull @NotEmpty
    private String issueString;
    @NotNull @NotEmpty
    private String label;
    @NotNull @NotEmpty
    private String type;
    @NotNull
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
     * Default constructor for MalletArticle.
     */
    public MalletArticle () { }

    /**
     * Constructor for MalletArticle with parameters.
     * @param id
     * @param issueString
     * @param label
     * @param type
     * @param topics
     * @param sectionId
     * @param sectionName
     * @param webPublicationDate
     * @param webTitle
     * @param webUrl
     * @param bodyText
     */
    public MalletArticle (String id, String issueString, String label, String type, List<String> topics, String sectionId, String sectionName, String webPublicationDate, String webTitle, String webUrl, String bodyText) {
        this.id = id;
        this.issueString = issueString;
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
     * @return the unique identifier of the article
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
     * @return the issue query associated with the article
     */
    public String getissueString() {
        return this.issueString;
    }

    /**
     * Sets the issue query associated with the article.
     * @param issueString
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
     * @return the label of the article
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Sets the label of the article.
     * @param label
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
     * @return the type of the article
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
     * Returns the list of topics associated with the article.
     * @return the list of topics associated with the article
     */
    public List<String> getTopics() {
        return this.topics;
    }

    /**
     * Sets the list of topics associated with the article.
     * @param topics
     * @throws IllegalArgumentException if the topics list is null
     */
    public void setTopics(List<String> topics) {
        if (topics == null) {
            throw new IllegalArgumentException("Topics cannot be null");
        }
        this.topics = topics;
    }

    /**
     * Returns the section ID of the article.
     * @return the section ID of the article
     */
    public String getSectionId() {
        return this.sectionId;
    }

    /**
     * Sets the section ID of the article.
     * @param sectionId
     * @throws IllegalArgumentException if the section ID is null or empty
     */
    public void setSectionId(String sectionId) {
        if (sectionId == null || sectionId.isEmpty()) {
            throw new IllegalArgumentException("Section ID cannot be null or empty");
        }
        this.sectionId = sectionId;
    }

    /**
     * Returns the section name of the article.
     * @return the section name of the article
     */
    public String getSectionName() {
        return this.sectionName;
    }

    /**
     * Sets the section name of the article.
     * @param sectionName
     * @throws IllegalArgumentException if the section name is null or empty
     */
    public void setSectionName(String sectionName) {
        if (sectionName == null || sectionName.isEmpty()) {
            throw new IllegalArgumentException("Section name cannot be null or empty");
        }
        this.sectionName = sectionName;
    }

    /**
     * Returns the web publication date of the article.
     * @return the web publication date of the article
     */
    public String getWebPublicationDate() {
        return this.webPublicationDate;
    }

    /**
     * Sets the web publication date of the article.
     * @param webPublicationDate
     * @throws IllegalArgumentException if the web publication date is null or empty
     */
    public void setWebPublicationDate(String webPublicationDate) {
        if (webPublicationDate == null || webPublicationDate.isEmpty()) {
            throw new IllegalArgumentException("Web publication date cannot be null or empty");
        }
        this.webPublicationDate = webPublicationDate;
    }

    /**
     * Returns the web title of the article.
     * @return the web title of the article
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
     * Returns the web URL of the article.
     * @return the web URL of the article
     */
    public String getWebUrl() {
        return this.webUrl;
    }

    /**
     * Sets the web URL of the article.
     * @param webUrl
     * @throws IllegalArgumentException if the web URL is null or empty
     */
    public void setWebUrl(String webUrl) {
        if (webUrl == null || webUrl.isEmpty()) {
            throw new IllegalArgumentException("Web URL cannot be null or empty");
        }
        this.webUrl = webUrl;
    }

    /**
     * Returns the body text of the article.
     * @return the body text of the article
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
