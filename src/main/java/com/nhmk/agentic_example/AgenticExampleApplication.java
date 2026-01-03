package com.nhmk.agentic_example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main Application Class
 * 
 * @EnableAsync: Enable async method execution support
 * Required cho @Async annotation trong StdAIService.storeConversationAsync()
 * Async storage của embeddings để không block chat response
 */
@EnableAsync
@SpringBootApplication
public class AgenticExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgenticExampleApplication.class, args);
	}

}
