/**
 * MonitoringRequest.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.monitoring.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class MonitoringRequest {
    @NotNull @NotEmpty
    private String issueQuery;
    @NotNull @NotEmpty
    private String label;
    @Pattern(regexp = "\\d{2}/\\d{2}/\\d{4}", message = "Start date must be in the format DD/MM/YYYY")
    private String startDate;
    @Pattern(regexp = "\\d{2}/\\d{2}/\\d{4}", message = "End date must be in the format DD/MM/YYYY")
    private String endDate;

    public MonitoringRequest() {
        // Default constructor
        // This is useful for frameworks that require a no-argument constructor
        // such as Spring when deserializing JSON requests.
    }

    /**
     * Default constructor for MonitoringRequest.
     * @param issueQuery the query to search for issues
     * @param label the label to filter issues
     * @param startDate the start date for the monitoring period
     * @param endDate the end date for the monitoring period
     */
    public MonitoringRequest(String issueQuery, String label, String startDate, String endDate) {      
        // Initialize the fields with the provided values
        this.issueQuery = issueQuery;
        this.label = label;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Returns the query to be used for searching issues.
     * @return the issue query string
     */
    public String getIssueQuery() {
        return this.issueQuery;
    }

    /**
     * Sets the query to be used for searching issues.
     * @param issueQuery
     */
    public void setIssueQuery(String issueQuery) {
        this.issueQuery = issueQuery;
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
    public String getStartDate() {
        return this.startDate;
    }

    /**
     * Sets the start date for the monitoring period.
     * @param startDate
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * Returns the end date for the monitoring period.
     * @return the end date string in format (DD/MM/YYYY)
     */
    public String getEndDate() {
        return this.endDate;
    }

    /**
     * Sets the end date for the monitoring period.
     * @param endDate
     */
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
