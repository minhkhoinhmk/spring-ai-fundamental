package com.nhmk.agentic_example.infrastructure.config;

import com.nhmk.agentic_example.infrastructure.advisor.TokenUsageAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleChatClientConfig {

    @Bean(name = "geminiChatClient")
    public ChatClient chatClient(@Qualifier("googleGenAiChatModel") ChatModel chatModel,
                                 @org.springframework.beans.factory.annotation.Qualifier("documentVectorStore") PgVectorStore documentVectorStore,
                                 @Value("${agentic_example.application.rag.top-k:10}") int ragTopK) {
        // Enable token usage tracking via custom advisor
        var tokenUsageAdvisor = new TokenUsageAdvisor();

        // Wire retrieval advisor using the document vector store (qualified bean)
        // Note: `documentVectorStore` bean is provided in PgVectorConfig and qualified as "documentVectorStore".
        // QuestionAnswerAdvisor will enrich requests with retrieved context from the document store.
        // Similarity threshold > 0.6 để lọc kết quả có độ tương đồng cao
        var searchRequest = new SearchRequest.Builder()
                .topK(ragTopK)
                .similarityThreshold(0.6)
                .build();

        var retrievalAdvisor = QuestionAnswerAdvisor.builder(documentVectorStore)
                .searchRequest(searchRequest)
                .build();

        return ChatClient.builder(chatModel)
                .defaultAdvisors(retrievalAdvisor, tokenUsageAdvisor)
                .build();
    }
}
