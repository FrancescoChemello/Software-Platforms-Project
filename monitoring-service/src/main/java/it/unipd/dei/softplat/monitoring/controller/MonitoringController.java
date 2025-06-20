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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    // For logging
    private static final Logger logger = LogManager.getLogger(MonitoringController.class);

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
            logger.error("Request cannot be null.");
            return ResponseEntity.badRequest().body("Request cannot be null.");
        }
        if (request.getissueString() == null || request.getissueString().isEmpty()) {
            logger.error("Issue query cannot be null or empty.");
            return ResponseEntity.badRequest().body("Issue query cannot be null or empty.");
        }
        if (request.getLabel() == null || request.getLabel().isEmpty()) {
            logger.error("Label cannot be null or empty.");
            return ResponseEntity.badRequest().body("Label cannot be null or empty.");
        }
        if (request.getStartDate() == null) {
            logger.error("Start date cannot be null.");
            return ResponseEntity.badRequest().body("Start date cannot be null.");
        }

        // Start the monitoring process
        monitoringService.startMonitoring(request);

        logger.info("Monitoring started for issue query: " + request.getissueString());

        return ResponseEntity.ok("Monitoring for issue query \""+request.getissueString()+"\" started successfully.");
    }
}
