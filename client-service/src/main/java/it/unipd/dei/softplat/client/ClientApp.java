/**
 * ClientApp.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.client;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import it.unipd.dei.softplat.client.service.ClientService;

@SpringBootApplication(scanBasePackages = {"it.unipd.dei.softplat"})
public class ClientApp {
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        var context = SpringApplication.run(ClientApp.class, args);
        System.out.println("Client Service is running...");
        
        // Get the Bean for ClientService from the application context
        var clientService = context.getBean(ClientService.class);
        boolean endInput = false;
        Scanner scanner = new Scanner(System.in);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

        System.out.println("Press ENTER to start: ");
        scanner.nextLine(); // Wait for the user to press ENTER
        
        System.out.println("This is a platform for monitoring articles from online newspapers using the Guardian Open Platform.");

        do {
            System.out.println("Please enter a command (type 'help' for a list of commands):");
            
            // Read user input
            String userInput = scanner.nextLine().trim();

            switch (userInput.toLowerCase()) {
                case "help":
                    System.out.println("Available commands are:"
                            + "\n- help: Show this help message"
                            + "\n- monitor: Start monitoring a new issue"
                            + "\n- query: Extract articles related to a specific issue"
                            + "\n- exit: Exit the application");
                    break;
                case "exit":
                    endInput = true;
                    break;
                case "monitor":
                    // Monitoring logic
                    System.out.println("Please enter IN ORDER the following details for the monitoring:"
                            +"\n1. The issue you want to monitor (e.g., artificial intelligence)"
                            +"\n2. A label for the issue (e.g., ai)"
                            +"\n3. The start date in ISO 8601 format (e.g., 2023-01-01T00:00:00Z)"
                            +"\n4. The end date in ISO 8601 format (e.g., 2023-12-31T23:59:59Z)"
                            +"\n\nNote: For continuous monitoring, write 'continuous' as the end date."
                    );
                    System.out.print("Enter the issue to monitor: ");
                    String issue = scanner.nextLine().trim();
                    System.out.print("Enter a label for the issue: ");
                    String label = scanner.nextLine().trim();
                    System.out.print("Enter the start date (yyyy-MM-dd'T'HH:mm:ss'Z): ");
                    Date startDate = null;
                    String startDateInput = scanner.nextLine().trim();
                    try {
                        TemporalAccessor startDateTemporal = formatter.parse(startDateInput); // Assuming the input is in a valid format
                        startDate = Date.from(Instant.from(startDateTemporal)); // Convert to Date
                    } catch (Exception e) {
                        System.out.println("Invalid start date format. Please use Please use (yyyy-MM-dd'T'HH:mm:ss'Z').");
                        continue; // Skip to the next iteration to re-prompt for input
                    }
                    System.out.print("Enter the end date (Please use yyyy-MM-dd'T'HH:mm:ss'Z') or 'continuous' for ongoing monitoring: ");
                    Date endDate = null;
                    String endDateInput = scanner.nextLine().trim();
                    if (!endDateInput.equalsIgnoreCase("continuous")) {
                        try {
                            TemporalAccessor endDateTemporal = formatter.parse(endDateInput); // Assuming the input is in a valid format
                            endDate = Date.from(Instant.from(endDateTemporal)); // Convert to Date
                        } catch (Exception e) {
                            System.out.println("Invalid end date format. Please use Please use (yyyy-MM-dd'T'HH:mm:ss'Z').");
                            continue; // Skip to the next iteration to re-prompt for input
                        }
                    } else if (endDateInput.equalsIgnoreCase("continuous")) {
                        endDate = null; // Set to null for continuous monitoring
                    }
                    // Validation of inputs
                    if (issue.isEmpty() || label.isEmpty()) {
                        System.out.println("Issue and label cannot be empty, please try again.");
                        continue; // Skip to the next iteration to re-prompt for input
                    }
                    if (startDate == null) {
                        System.out.println("Start date cannot be null, please try again.");
                        continue; // Skip to the next iteration to re-prompt for input
                    }
                    if (endDate != null && startDate.after(endDate)) {
                        System.out.println("Start date cannot be after end date, please try again.");
                        continue; // Skip to the next iteration to re-prompt for input
                    }
                    // Call the service to start the monitoring
                    clientService.sendMonitoringRequest(issue, label, startDate, endDate);
                    // Wait for the monitoring to complete
                    System.out.println("Monitoring request sent. Waiting for the monitoring to complete...");
                    System.out.println("Press ENTER to continue.");
                    scanner.nextLine(); // Wait for the user to press ENTER
                    break;
                case "query":
                    // Query logic
                    System.out.println("Please enter IN ORDER the following details for the query:"
                            + "\n1. The query topic (e.g., 'ChatGPT')"
                            + "\n2. The issue corpus (e.g., 'artificial intelligence')"
                            + "\n3. The number of topics to extract (e.g., 5)"
                            + "\n4. The number of top words per topic (e.g., 10)"
                            + "\n5. The start date in ISO 8601 format (e.g., 2023-01-01T00:00:00Z) OPTIONAL"
                            + "\n6. The end date in ISO 8601 format (e.g., 2023-12-31T23:59:59Z) OPTIONAL"
                    );
                    System.out.print("Enter the query topic: ");
                    String queryTopic = scanner.nextLine().trim();
                    System.out.print("Enter the issue corpus: ");
                    String issueCorpus = scanner.nextLine().trim();
                    System.out.print("Enter the number of topics to extract: ");
                    int numTopics;
                    try {
                        numTopics = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number format for topics. Please enter a positive integer.");
                        continue; // Skip to the next iteration to re-prompt for input
                    }
                    System.out.print("Enter the number of top words per topic: ");
                    int numTopWordsPerTopic;
                    try {
                        numTopWordsPerTopic = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number format for top words per topic. Please enter a positive integer.");
                        continue; // Skip to the next iteration to re-prompt for input
                    }
                    System.out.print("Enter the start date (yyyy-MM-dd'T'HH:mm:ss'Z') or leave empty for no start date: ");
                    Date startQueryDate = null;
                    String startQueryDateInput = scanner.nextLine().trim();
                    if (!startQueryDateInput.isEmpty()) {
                        try {
                            TemporalAccessor startQueryDateTemporal = formatter.parse(startQueryDateInput); // Assuming the input is in a valid format
                            startQueryDate = Date.from(Instant.from(startQueryDateTemporal)); // Convert to Date
                        } catch (Exception e) {
                            System.out.println("Invalid start date format. Please use Please use (yyyy-MM-dd'T'HH:mm:ss'Z').");
                            continue; // Skip to the next iteration to re-prompt for input
                        }
                    }
                    System.out.print("Enter the end date (yyyy-MM-dd'T'HH:mm:ss'Z') or leave empty for no end date: ");
                    Date endQueryDate = null;
                    String endQueryDateInput = scanner.nextLine().trim();
                    if (endQueryDateInput != null && !endQueryDateInput.isEmpty()) {
                        try {
                            TemporalAccessor endQueryDateTemporal = formatter.parse(endQueryDateInput); // Assuming the input is in a valid format
                            endQueryDate = Date.from(Instant.from(endQueryDateTemporal)); // Convert to Date
                        } catch (Exception e) {
                            System.out.println("Invalid end date format. Please use Please use (yyyy-MM-dd'T'HH:mm:ss'Z').");
                            continue; // Skip to the next iteration to re-prompt for input
                        }
                    }
                    // Validation of inputs
                    if (queryTopic.isEmpty() || issueCorpus.isEmpty()) {
                        System.out.println("Query topic and issue corpus cannot be empty, please try again.");
                        continue; // Skip to the next iteration to re-prompt for input
                    }
                    if (numTopics <= 0 || numTopWordsPerTopic <= 0) {
                        System.out.println("Number of topics and top words per topic must be positive integers, please try again.");
                        continue; // Skip to the next iteration to re-prompt for input
                    }
                    if (startQueryDate != null && endQueryDate != null && startQueryDate.after(endQueryDate)) {
                        System.out.println("Start date cannot be after end date, please try again.");
                        continue; // Skip to the next iteration to re-prompt for input
                    }
                    // Call the service to send the query request
                    clientService.sendQueryRequest(queryTopic, issueCorpus, numTopics, numTopWordsPerTopic, startQueryDate, endQueryDate);
                    // Wait for the query to complete
                    System.out.println("Query request sent. Waiting for the results...");
                    System.out.println("Press ENTER to continue.");
                    scanner.nextLine(); // Wait for the user to press ENTER
                    break;
                default:
                    System.out.println("Unknown command. Type 'help' for assistance.");
                    break;
            }
        } while(!endInput);
        // Close the scanner to prevent resource leaks
        scanner.close();
        System.out.println("Goodbye!");
    }
}
