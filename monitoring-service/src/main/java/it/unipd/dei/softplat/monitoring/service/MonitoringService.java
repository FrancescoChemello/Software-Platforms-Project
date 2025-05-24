/**
 * MonitoringController.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.monitoring.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
// Client Java for The Guardian Open Platform API
import com.apitheguardian.GuardianContentApi;
import com.apitheguardian.bean.Response;
import com.mashape.unirest.http.exceptions.UnirestException;

import it.unipd.dei.softplat.monitoring.model.MonitoringRequest;

@Service
public class MonitoringService {

    private GuardianContentApi client;
    
    public MonitoringService(@Value("${guardian.open.api.key}") String apiKey) {

        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("The Guardian Service API environment variable is not set.");
        }

        this.client = new GuardianContentApi(apiKey);
    }

    /**
     * Starts the monitoring process for the given request.
     * @param request
     */
    public void startMonitoring(MonitoringRequest request) {

        Response response;

        if (request == null) {
            throw new IllegalArgumentException("Monitoring request cannot be null.");
        }

        // To manage date formats
        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        try {
            client.setFromDate(dateFormat.parse(request.getStartDate()));
            client.setToDate(dateFormat.parse(request.getEndDate()));
        }
        catch (ParseException e){
            throw new IllegalArgumentException("Invalid date format. Please use dd/MM/yyyy.");
        }

        // Set the label for the query
        try{
            response = client.getContent(request.getIssueQuery());
        }
        catch (UnirestException e){
            throw new RuntimeException("Error while fetching content from The Guardian API: " + e.getMessage(), e);
        }

        // Here you can implement the logic to process the response and store it in the database
        if (response.getResults().length == 0) {
            System.out.println("No articles found for the given query and date range.");
        } else {
            System.out.println("Found " + response.getResults().length + " articles.");
        }

        Arrays.stream(response.getResults()).forEach(System.out::println);

        // JSON format of the response
    }
}
