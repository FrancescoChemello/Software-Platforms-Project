/**
 * MonitoringRequest.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.monitoring.model;

import java.util.Date;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * This class represents a request for monitoring issues.
 * It contains fields for the issue query, label, start date, and end date.
 * The class includes validation annotations to ensure that the fields are not null or empty,
 * and that the date fields follow the specified format (DD/MM/YYYY).
 */
public class MonitoringRequest {
    @NotNull @NotEmpty
    private String issueString;
    @NotNull @NotEmpty
    private String label;
    @NotNull
    private Date startDate;
    private Date endDate;

    /**
     * Default constructor for MonitoringRequest.
     * This constructor is required for frameworks that require a no-argument constructor,
     * such as Spring when deserializing JSON requests.
     */
    public MonitoringRequest() { }

    /**
     * Default constructor for MonitoringRequest.
     * @param issueString the query to search for issues
     * @param label the label to filter issues
     * @param startDate the start date for the monitoring period
     * @param endDate the end date for the monitoring period
     */
    public MonitoringRequest(String issueString, String label, Date startDate, Date endDate) {      
        // Initialize the fields with the provided values
        this.issueString = issueString;
        this.label = label;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Returns the query to be used for searching issues.
     * @return the issue query string
     */
    public String getissueString() {
        return this.issueString;
    }

    /**
     * Sets the query to be used for searching issues.
     * @param issueString
     */
    public void setissueString(String issueString) {
        this.issueString = issueString;
    }

    /**
     * Returns the label to filter issues.
     * @return the label string
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Sets the label to filter issues.
     * @param label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Returns the start date for the monitoring period.
     * @return the start date string in format (DD/MM/YYYY)
     */
    public Date getStartDate() {
        return this.startDate;
    }

    /**
     * Sets the start date for the monitoring period.
     * @param startDate
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Returns the end date for the monitoring period.
     * @return the end date string in format (DD/MM/YYYY)
     */
    public Date getEndDate() {
        return this.endDate;
    }

    /**
     * Sets the end date for the monitoring period.
     * @param endDate
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
