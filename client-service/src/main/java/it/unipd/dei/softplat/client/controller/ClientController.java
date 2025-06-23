/**
 * ClientController.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.client.controller;

import jakarta.validation.Valid;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import it.unipd.dei.softplat.client.dto.MessageDTO;
import it.unipd.dei.softplat.client.model.QueryResult;
import it.unipd.dei.softplat.client.model.QueryTopic;
import it.unipd.dei.softplat.client.service.ClientService;

@RestController
public class ClientController {

    private final ClientService clientService;

    // For logging
    private static final Logger logger = LogManager.getLogger(ClientController.class);

    /**
     * Default constructor for ClientController.
     * @param clientService The service to handle client operations.
     */
    @Autowired
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }
    
    /**
     * This method handles the query result sent by the client.
     * It validates the query result and processes it using the client service.
     * @param queryResult
     * @return
     */
    @PostMapping("/client/query-result/")
    public ResponseEntity<?> getQueryResult(@Valid @RequestBody QueryResult queryResult) {
        if (queryResult == null) {
            logger.error("Query result cannot be null.");
            return ResponseEntity.badRequest().body("Query result cannot be null.");
        }
        String query = queryResult.getQuery();
        if (query == null || query.isEmpty()) {
            logger.error("Query cannot be null or empty.");
            return ResponseEntity.badRequest().body("Query cannot be null or empty.");
        }
        if (queryResult.getTopics() == null) {
            logger.error("Topics cannot be null.");
            return ResponseEntity.badRequest().body("Topics cannot be null.");
        }
        ArrayList<QueryTopic> topics = new ArrayList<>(queryResult.getTopics());

        // Call the service to process the query result.
        clientService.processQueryResult(query, topics);
        logger.info("Query result processed successfully for query: " + query);
        return ResponseEntity.ok("Query result processed successfully.");
    }

    /**
     * This method handles the status message sent by the client.
     * It validates the message and processes it using the client service.
     * @param messageDTO
     * @return
     */
    @PostMapping("/client/status/")
    public ResponseEntity<?> getStatus (@Valid @RequestBody MessageDTO messageDTO) {
        if (messageDTO == null) {
            logger.error("Message cannot be null.");
            return ResponseEntity.badRequest().body("Message cannot be null.");
        }
        String message = messageDTO.getMessage();
        if (message == null || message.isEmpty()) {
            logger.error("Message cannot be null or empty.");
            return ResponseEntity.badRequest().body("Message cannot be null or empty.");
        }
        String status = messageDTO.getStatus();
        if (status == null || status.isEmpty()) {
            logger.error("Status cannot be null or empty.");
            return ResponseEntity.badRequest().body("Status cannot be null or empty.");
        }
        
        clientService.processMessageStatus(status, message);
        logger.info("Status message processed successfully: " + message);
        return ResponseEntity.ok("Status message processed successfully.");
    }
}
