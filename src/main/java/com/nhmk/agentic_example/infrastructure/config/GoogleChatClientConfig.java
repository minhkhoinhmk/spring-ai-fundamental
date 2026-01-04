package com.nhmk.agentic_example.infrastructure.config;

import com.nhmk.agentic_example.application.extract.PageExtractionFacade;
import com.nhmk.agentic_example.infrastructure.advisor.TokenUsageAdvisor;
import com.nhmk.agentic_example.infrastructure.webextraction.BoilerpipeReadabilityExtractor;
import com.nhmk.agentic_example.infrastructure.webextraction.JsoupPageFetcher;
import com.nhmk.agentic_example.infrastructure.webextraction.PageExtractionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class GoogleChatClientConfig {

    private final ResourceLoader resourceLoader;
    @Value("${app.extract.timeout-ms:5000}")
    private int fetchTimeoutMs;
    @Value("${app.extract.user-agent:Mozilla/5.0 (compatible; AgenticExample/1.0)}")
    private String fetchUserAgent;
    @Value("${app.extract.max-chars:300}")
    private int maxExtractChars;
    @Value("${app.conversation.max-messages:20}")
    private int maxMessages;

    public GoogleChatClientConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

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

    @Bean
    public ChatMemory chatMemory() {
        // Use message window memory with configurable max messages; repository defaults to in-memory
        // Giữ nguyên in-memory ChatMemory cho recent messages (fast access)
        // VectorStore sẽ được dùng song song cho semantic search của older messages
        return MessageWindowChatMemory.builder().maxMessages(maxMessages).build();
    }

    @Bean
    public JsoupPageFetcher jsoupPageFetcher() {
        return new JsoupPageFetcher(fetchTimeoutMs, fetchUserAgent);
    }

    @Bean
    public BoilerpipeReadabilityExtractor boilerpipeReadabilityExtractor() {
        return new BoilerpipeReadabilityExtractor();
    }

    @Bean
    public PageExtractionService pageExtractionService(JsoupPageFetcher fetcher, BoilerpipeReadabilityExtractor extractor) {
        return new PageExtractionService(fetcher, extractor, maxExtractChars);
    }

    @Bean
    public PageExtractionFacade pageExtractionFacade(PageExtractionService service) {
        return new PageExtractionFacade(service);
    }
}
