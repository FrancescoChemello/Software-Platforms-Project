/**
 * MongodbApp.java
 * This file implements the MongodbApp class
 * which is responsible for starting the MongoDB service.
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.mongodb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This class is intended to start the MongoDB Service application.
 */
@SpringBootApplication
public class MongodbApp {

    public static void main(String[] args) {
        SpringApplication.run(MongodbApp.class, args);
        System.out.println("MongoDB Service is running...");
    }

}
