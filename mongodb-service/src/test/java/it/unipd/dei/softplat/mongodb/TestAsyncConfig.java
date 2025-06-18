/**
 * TestAsyncConfig.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.mongodb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.Executor;

/**
 * This class is intended to configure the asynchronous execution of tasks in tests.
 */
@Configuration
public class TestAsyncConfig {
    @Bean
    public Executor taskExecutor() {
        // Run every task synchronously in the same thread
        return Runnable::run;
    }
}
