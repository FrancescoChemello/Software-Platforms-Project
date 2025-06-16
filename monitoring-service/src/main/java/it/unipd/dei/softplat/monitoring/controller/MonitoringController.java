/**
 * MonitoringController.java
 * This file implements the MonitoringController class
 * which is responsible for handling monitoring-related requests.
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */


package it.unipd.dei.softplat.monitoring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import it.unipd.dei.softplat.monitoring.model.MonitoringRequest;
import it.unipd.dei.softplat.monitoring.service.MonitoringService;
import jakarta.validation.Valid;

/**
 * This class is intended to handle requests related to monitoring.
 * It provides an endpoint to start monitoring for a specific issue query.
 * The request must include the issue query, label, start date, and end date.
 */
@RestController
public class MonitoringController {

    private final MonitoringService monitoringService;

    /**
     * Default constructor for MonitoringController.
     */
    @Autowired
    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    /**
     * Handles the request to start monitoring for a specific issue query.
     * @param request The request containing the issue query, label, start date, and end date.
     * @return A ResponseEntity indicating the result of the operation.
     */
    @PostMapping("/monitoring/start/")
    public ResponseEntity<?> startMonitoring(@Valid @RequestBody MonitoringRequest request){
        // Validate the request
        if (request == null) {
            return ResponseEntity.badRequest().body("Request cannot be null.");
        }
        if (request.getIssueQuery() == null || request.getIssueQuery().isEmpty()) {
            return ResponseEntity.badRequest().body("Issue query cannot be null or empty.");
        }
        if (request.getLabel() == null || request.getLabel().isEmpty()) {
            return ResponseEntity.badRequest().body("Label cannot be null or empty.");
        }
        if (request.getStartDate() == null) {
            return ResponseEntity.badRequest().body("Start date cannot be null.");
        }

        // Start the monitoring process
        monitoringService.startMonitoring(request);
        return ResponseEntity.ok("Monitoring for issue query \""+request.getIssueQuery()+"\" started successfully.");
    }
}
