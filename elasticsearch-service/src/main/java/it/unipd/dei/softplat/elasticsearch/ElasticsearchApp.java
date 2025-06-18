/**
 * ElasticsearchApp.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.elasticsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * This class is intended to start the Elasticsearch Service application.
 */
@SpringBootApplication(scanBasePackages = {"it.unipd.dei.softplat"})
public class ElasticsearchApp {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
 
    public static void main(String[] args) {
        SpringApplication.run(ElasticsearchApp.class, args);
        System.out.println("Elasticsearch Service is running...");
    }
}
