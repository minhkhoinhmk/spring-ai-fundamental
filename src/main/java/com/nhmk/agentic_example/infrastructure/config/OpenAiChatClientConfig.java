package com.nhmk.agentic_example.infrastructure.config;

import com.nhmk.agentic_example.infrastructure.advisor.TokenUsageAdvisor;
import com.nhmk.agentic_example.infrastructure.tools.CalculatorTool;
import com.nhmk.agentic_example.infrastructure.tools.ExchangeRateTool;
import com.nhmk.agentic_example.infrastructure.tools.TavilySearchTool;
import com.nhmk.agentic_example.infrastructure.tools.NewsApiTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình ChatClient và ChatModel cho OpenAI, tách biệt với Google GenAI.
 * Tuân thủ DDD: config riêng biệt, dễ maintain, dễ mở rộng.
 */
@Configuration
public class OpenAiChatClientConfig {
    @Bean(name = "openAiChatClient")
    public ChatClient openAiChatClient(@Qualifier("openAiChatModel") ChatModel openAiChatModel,
                                       @org.springframework.beans.factory.annotation.Qualifier("chatHistoryVectorStore") PgVectorStore vectorStore,
                                       CalculatorTool calculatorTool,
                                       TavilySearchTool tavilySearchTool,
                                       NewsApiTool newsApiTool,
                                       ExchangeRateTool exchangeRateTool) {
        var vectorMemoryAdvisor = VectorStoreChatMemoryAdvisor.builder(vectorStore)
                .build();
        var tokenUsageAdvisor = new TokenUsageAdvisor();

        return ChatClient.builder(openAiChatModel)
                .defaultTools(calculatorTool, tavilySearchTool, newsApiTool, exchangeRateTool)
                .defaultAdvisors(vectorMemoryAdvisor, tokenUsageAdvisor)
                .build();
    }
}
