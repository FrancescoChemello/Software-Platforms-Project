/**
 * MongodbApp.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */


package it.unipd.dei.softplat.mongodb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MongodbApp {

    public static void main(String[] args) {
        SpringApplication.run(MongodbApp.class, args);
        System.out.println("MongoDB Service is running...");
    }

}
