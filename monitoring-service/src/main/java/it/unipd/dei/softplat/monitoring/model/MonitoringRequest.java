/**
 * MonitoringRequest.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.monitoring.model;

public class MonitoringRequest {
    private String issueQuery;
    private String label;
    private String startDate;
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
        // Check the validity of the input parameters
        if(issueQuery == null || issueQuery.isEmpty()) {
            throw new IllegalArgumentException("Issue query cannot be null or empty.");
        }
        if(label == null || label.isEmpty()) {
            throw new IllegalArgumentException("Label cannot be null or empty.");
        }
        if(startDate == null || startDate.isEmpty()) {
            throw new IllegalArgumentException("Start date cannot be null or empty.");
        }else{
            // Validate the date format (DD/MM/YYYY)
            if(!startDate.matches("\\d{2}/\\d{2}/\\d{4}")) {
                throw new IllegalArgumentException("Start date must be in the format DD/MM/YYYY.");
            }
        }
        if(endDate == null || endDate.isEmpty()) {
            throw new IllegalArgumentException("End date cannot be null or empty.");
        }else{
            // Validate the date format (DD/MM/YYYY)
            if(!endDate.matches("\\d{2}/\\d{2}/\\d{4}")) {
                throw new IllegalArgumentException("End date must be in the format DD/MM/YYYY.");
            }
        }
        // Check if the start date is before the end date
        if(startDate.compareTo(endDate) > 0) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }
        
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
        return issueQuery;
    }

    /**
     * Sets the query to be used for searching issues.
     * @param issueQuery
     */
    public void setIssueQuery(String issueQuery) {
        if(issueQuery == null || issueQuery.isEmpty()) {
            throw new IllegalArgumentException("Issue query cannot be null or empty.");
        }
        this.issueQuery = issueQuery;
    }

    /**
     * Returns the label to filter issues.
     * @return the label string
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label to filter issues.
     * @param label
     */
    public void setLabel(String label) {
        if(label == null || label.isEmpty()) {
            throw new IllegalArgumentException("Issue query cannot be null or empty.");
        }
        this.label = label;
    }

    /**
     * Returns the start date for the monitoring period.
     * @return the start date string in format (DD/MM/YYYY)
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date for the monitoring period.
     * @param startDate
     */
    public void setStartDate(String startDate) {
        if(startDate == null || startDate.isEmpty()) {
            throw new IllegalArgumentException("Start date cannot be null or empty.");
        }else{
            // Validate the date format (DD/MM/YYYY)
            if(!startDate.matches("\\d{2}/\\d{2}/\\d{4}")) {
                throw new IllegalArgumentException("Start date must be in the format DD/MM/YYYY.");
            }
        }
        // Check if the start date is before the end date
        if(startDate.compareTo(getEndDate()) > 0) {
            throw new IllegalArgumentException("Start date cannot be after date: "+getEndDate()+".");
        }
        this.startDate = startDate;
    }

    /**
     * Returns the end date for the monitoring period.
     * @return the end date string in format (DD/MM/YYYY)
     */
    public String getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date for the monitoring period.
     * @param endDate
     */
    public void setEndDate(String endDate) {
        if(endDate == null || endDate.isEmpty()) {
            throw new IllegalArgumentException("Start date cannot be null or empty.");
        }else{
            // Validate the date format (DD/MM/YYYY)
            if(!endDate.matches("\\d{2}/\\d{2}/\\d{4}")) {
                throw new IllegalArgumentException("Start date must be in the format DD/MM/YYYY.");
            }
        }
        // Check if the start date is before the end date
        if(getStartDate().compareTo(endDate) > 0) {
            throw new IllegalArgumentException("End date cannot be before date: "+getStartDate()+".");
        }
        this.endDate = endDate;
    }
}
