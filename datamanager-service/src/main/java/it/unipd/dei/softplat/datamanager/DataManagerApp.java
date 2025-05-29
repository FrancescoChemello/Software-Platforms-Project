/**
 * DataManagerApp.java
 * This file implements the DataManagerApp class
 * which is responsible for starting the Data Manager service.
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.datamanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This class is intended to start the Data Manager Service application.
 */
@SpringBootApplication
public class DataManagerApp {
    
    public static void main(String[] args){
        SpringApplication.run(DataManagerApp.class, args);
        System.out.println("Data Manager Service is running...");
    }
}
