/**
 * MalletApp.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.mallet;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * This class is intended to start the Mallet Service application.
 */
@SpringBootApplication
public class MalletApp {
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(MalletApp.class, args);
        System.out.println("Mallet Service is running...");
    }
}
