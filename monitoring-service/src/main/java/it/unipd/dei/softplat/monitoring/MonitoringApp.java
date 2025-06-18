/**
 * MonitoringController.java
 * This file implements the MonitoringController class
 * which is responsible for handling monitoring-related requests.
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * This class is intended to start the Monitoring Service application.
 */
@SpringBootApplication(scanBasePackages = {"it.unipd.dei.softplat"})
public class MonitoringApp {
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    } 

    public static void main(String[] args){
        SpringApplication.run(MonitoringApp.class, args);
        System.out.println("Monitoring Service is running...");
    }
}
