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
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import it.unipd.dei.softplat.client.model.QueryResult;
import it.unipd.dei.softplat.client.model.QueryTopic;
import it.unipd.dei.softplat.client.service.ClientService;

@SpringBootApplication(scanBasePackages = {"it.unipd.dei.softplat"})
public class ClientApp {

    // BlockingQueue to hold the query results
    public static final BlockingQueue<QueryResult> resultQueue = new ArrayBlockingQueue<>(1);
    // BlockingQueue to hold the monitoring status
    public static final BlockingQueue<String> monitoringQueue = new ArrayBlockingQueue<>(1);
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Utility method to normalize a string by converting it to lowercase,
     * replacing spaces with underscores, and removing any non-alphanumeric
     * @param input
     * @return
     */
    public static String normalizeString(Scanner scanner, String prompt) {
        String input = "";
        do {
            System.out.println(prompt);
            input = scanner.nextLine().trim();
            input = input.toLowerCase()
                .replaceAll("\\s+", "_") // Replace spaces with underscores
                .replaceAll("[^a-z0-9_]", ""); // Remove any non-alphanumeric characters except underscores
            if (input.isEmpty()) {
                System.out.println("The label cannot contain ONLY special characters or spaces. Please enter a valid one.");
            }
        } while (input.isEmpty());
        
        return input;
    }

    /**
     * Utility method to read a non-empty string from the user input.
     * @param scanner
     * @param prompt
     * @return
     */
    public static String readNonEmptyString(Scanner scanner, String prompt) {
        String input = "";
        do {
            System.out.println(prompt);
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Input cannot be empty. Please try again.");
            }
        } while (input.isEmpty());
        return input;
    }

    /**
     * Utility method to read an integer value from the user input.
     * @param scanner
     * @param prompt
     * @return
     */
    public static int readIntValues(Scanner scanner, String prompt) {
        int integerNumber = 0;
        do {
            System.out.println(prompt);
            try {
                integerNumber = Integer.parseInt(scanner.nextLine().trim());
            }
            catch (NumberFormatException e) {
                System.out.println("Invalid number, please enter a positive integer.");
            }
        } while (integerNumber <= 0);

        return integerNumber;
    }

    /**
     * Utility method to read a date from the user input.
     * The date should be in ISO 8601 format (yyyy-MM-dd'T'HH:mm:ss'Z').
     * @param scanner
     * @param prompt
     * @param isRequired
     * @return
     */
    public static Date readDate(Scanner scanner, String prompt, boolean isRequired) {
        Date date = null;
        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        do {
            System.out.println(prompt);
            String dateInput = scanner.nextLine().trim();
            if (dateInput.isEmpty()) {  // The user just pressed ENTER
                if (!isRequired) {
                    return null; // Return null, the date is not mandatory
                } else {
                    System.out.println(("Date is required. Please enter a valid date in ISO 8601 format (yyyy-MM-dd'T'HH:mm:ss'Z')."));
                }
            } else {  // The user entered a date
                try {
                    TemporalAccessor ta = formatter.parse(dateInput);
                    Instant instant = Instant.from(ta);
                    date = Date.from(instant);

                    // Check if the date is in the future
                    if (date.after(new Date())) {
                        System.out.println("The date cannot be in the future. Please enter a valid date in ISO 8601 format (yyyy-MM-dd'T'HH:mm:ss'Z').");
                        date = null; // Reset date to null to re-prompt
                    }
                }
                catch (Exception e) {
                    System.out.println("Invalid date format. Please enter a valid date in ISO 8601 format (yyyy-MM-dd'T'HH:mm:ss'Z').");
                    date = null; // Reset date to null to re-prompt
                }
            }
        } while (date == null);

        // Valid date is entered, return it
        return date;
    }

    public static void main(String[] args) {
        var context = SpringApplication.run(ClientApp.class, args);
        System.out.println("Client Service is running...");
        
        // Get the Bean for ClientService from the application context
        var clientService = context.getBean(ClientService.class);
        boolean endInput = false;
        boolean monitoringStatus = false;
        boolean apiStatus = false;
        String issue = "";
        String label = "";
        // Create a Scanner to read user input
        Scanner scanner = new Scanner(System.in);

        System.out.println("Press ENTER to start: ");
        scanner.nextLine(); // Wait for the user to press ENTER
        
        System.out.println("This is a platform for monitoring articles from online newspapers using the Guardian Open Platform.");

        do {
            try {
                System.out.println("Please enter a command (type 'help' for a list of commands):");
                
                // Read user input
                String userInput = scanner.nextLine().trim();

                switch (userInput.toLowerCase()) {

                    case "help":

                        System.out.println("Available commands are:"
                                + "\n- help: Show this help message"
                                + "\n- monitor: Start monitoring a new issue"
                                + "\n- query: Extract articles related to a specific issue"
                                + "\n- status: Check the status of the monitoring and API rate limit"
                                + "\n- exit: Exit the application");
                        break;

                    case "exit":

                        endInput = true;
                        System.out.println("Goodbye!");
                        break;

                    case "status":

                        // Check the status of the monitoring and API rate limit
                        monitoringStatus = clientService.monitoringStatus();
                        apiStatus = clientService.apiRateLimitStatus();
                        if (monitoringStatus) {
                            System.out.println("Monitoring is enabled for issue query <" + issue + "> with issue corpus <" + label +">.");
                        } else {
                            System.out.println("Monitoring is not enabled. Please start monitoring an issue first.");
                        
                        }
                        if (apiStatus) {
                            System.out.println("API rate limit is exceeded. Please try again tomorrow.");
                        } else {
                            System.out.println("API rate limit is within the allowed limits.");
                        }
                        break;

                    case "monitor":

                        // Check if monitoring is already enabled
                        monitoringStatus = clientService.monitoringStatus();
                        if (monitoringStatus) {
                            System.out.println("Monitoring is already enabled. You can query the articles related to the monitored issue <" + issue + "> with issue corpus <" + label + ">.");
                            System.out.println("If you want to monitor a new issue, please restart the application.");
                            continue; // Skip
                        }

                        // Monitoring logic
                        System.out.println("Please enter IN ORDER the following details for the monitoring:"
                                +"\n1. The issue you want to monitor (e.g., artificial intelligence)"
                                +"\n2. A label for the issue corpus (e.g., ai)"
                                +"\n3. The start date in ISO 8601 format (e.g., 2023-01-01T00:00:00Z)"
                                +"\n4. The end date in ISO 8601 format (e.g., 2023-12-31T23:59:59Z)"
                                +"\n\nNote: For continuous monitoring, write 'continuous' as the end date."
                        );

                        issue = readNonEmptyString(scanner, "Enter the issue to monitor (use \"...\" to exact match): ");

                        label = normalizeString(scanner, "Enter a label for the issue corpus: ");

                        Date startDate = readDate(scanner, "Enter the start date (yyyy-MM-dd'T'HH:mm:ss'Z): ", true);

                        Date endDate = null;
                        do {
                            endDate = readDate(scanner, "Enter the end date (Please use yyyy-MM-dd'T'HH:mm:ss'Z') or leave it empty for ongoing monitoring: ", false);
                            if (endDate != null && endDate.before(startDate)) {
                                System.out.println("End date cannot be before start date. Please enter a valid end date.");
                            }
                        } while (endDate != null && endDate.before(startDate));
                        
                        monitoringQueue.clear(); // Clear the queue
                        // Call the service to start the monitoring
                        clientService.sendMonitoringRequest(issue, label, startDate, endDate);
                        // Wait for the monitoring to complete
                        System.out.println("Monitoring request sent. Waiting for confirmation...");
                        String monitoringConfirm = monitoringQueue.take(); // Wait for the status from the queue
                        // Read the status from the queue
                        if (monitoringConfirm.equals("Monitoring is enabled")) {
                            System.out.println("Monitoring started successfully for issue: " + issue);
                            System.out.println("You can now query the articles related to this issue.");
                            System.out.println("Full monitoring will require some time, please be patient.");
                        } else if (monitoringConfirm.equals("API rate limit exceeded")) {
                            System.out.println("API rate limit exceeded. Please try again tomorrow.");
                            System.exit(0); // Exit the application
                        }
                        monitoringQueue.clear(); // Clear the queue for the next monitoring request
                        break;

                    case "query":

                        // Query logic
                        monitoringStatus = clientService.monitoringStatus();
                        apiStatus = clientService.apiRateLimitStatus();
                        if (!monitoringStatus) {
                            System.out.println("Monitoring is not enabled. Please start monitoring an issue first.");
                            continue; // Skip to the next iteration to re-prompt for input
                        }
                        if (apiStatus && !monitoringStatus) {
                            System.out.println("API rate limit exceeded and no monitoring issue is enabled. Please try again tomorrow.");
                            endInput = true;
                            continue;
                        }
                        if (apiStatus && monitoringStatus) {
                            System.out.println("API rate limit exceeded, but monitoring is enabled. You can still query the articles, but results may be limited.");
                            System.out.println("Please try again tomorrow for full results.");
                        }

                        System.out.println("Please enter IN ORDER the following details for the query:"
                                + "\n1. The query topic (e.g., 'ChatGPT')"
                                + "\n2. The issue corpus (e.g., 'ai')"
                                + "\n3. The number of topics to extract (e.g., 5)"
                                + "\n4. The number of top words per topic (e.g., 10)"
                                + "\n5. The start date in ISO 8601 format (e.g., 2023-01-01T00:00:00Z) OPTIONAL"
                                + "\n6. The end date in ISO 8601 format (e.g., 2023-12-31T23:59:59Z) OPTIONAL"
                        );

                        String queryTopic = readNonEmptyString(scanner, "Enter the query topic (use \"...\" to exact match): ");

                        int numTopics = readIntValues(scanner, "Enter the number of topics to extract: ");

                        int numTopWordsPerTopic = readIntValues(scanner, "Enter the number of top words per topic: ");
                        
                        Date startQueryDate = readDate(scanner, "Enter the start date (yyyy-MM-dd'T'HH:mm:ss'Z') or leave empty for no start date: ", false);
                        
                        Date endQueryDate = null;
                        do {
                            endQueryDate = readDate(scanner, "Enter the end date (yyyy-MM-dd'T'HH:mm:ss'Z') or leave empty for no end date: ", false);
                            if (startQueryDate != null && endQueryDate != null && startQueryDate.after(endQueryDate)) {
                                System.out.println("End date cannot be before start date. Please enter a valid end date.");
                            }
                        } while (startQueryDate != null && endQueryDate != null && startQueryDate.after(endQueryDate));
                        
                        // Call the service to send the query request
                        clientService.sendQueryRequest(queryTopic, label, numTopics, numTopWordsPerTopic, startQueryDate, endQueryDate);
                        System.out.println("Query sent. Waiting for results...");
                        QueryResult result = resultQueue.take(); // Wait for the result from the queue
                        clientService.processQueryResult(result.getQuery(), new ArrayList<>(result.getTopics()));
                        // Read the result from the queue
                        System.out.println("Result for query: " + result.getQuery() + "\n");
                        if (result.getQuery().isEmpty() || result.getTopics().size() == 0) {
                            System.out.println("No articles found for the query: " + result.getQuery());
                            System.out.println("Please try a different query.");
                        } else {
                            System.out.println("Found " + result.getTopics().size() + " articles for the query: " + result.getQuery());
                            for (QueryTopic topic : result.getTopics()) {
                                System.out.println("Article ID: " + topic.getId());
                                System.out.println("Top words: " + String.join(", ", topic.getTopWords()));
                                System.out.println();
                            }
                        }
                        resultQueue.clear(); // Clear the queue for the next query    
                        break;
                    
                    default:
                        
                        System.out.println("Unknown command. Type 'help' for assistance.");
                        break;
                }
            }
            // Handle any unexpected exceptions
            catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
                endInput = true;
            }
        } while(!endInput);
        // Close the scanner to prevent resource leaks
        scanner.close();
    }
}
