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

@SpringBootApplication
public class MonitoringApp {
    
    public static void main(String[] args){
        SpringApplication.run(MonitoringApp.class, args);
        System.out.println("Monitoring Service is running...");
    }
}
