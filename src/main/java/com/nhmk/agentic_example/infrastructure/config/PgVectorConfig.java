package com.nhmk.agentic_example.infrastructure.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class PgVectorConfig {
    @Bean
    @Qualifier("documentVectorStore")
    public PgVectorStore documentVectorStore(JdbcTemplate jdbcTemplate, @Qualifier("googleGenAiTextEmbedding") EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .vectorTableName("document_vectors")
                .dimensions(768)
                .initializeSchema(true)
                .build();
    }

    @Bean
    @Qualifier("chatHistoryVectorStore")
    public PgVectorStore chatHistoryVectorStore(JdbcTemplate jdbcTemplate, @Qualifier("openAiEmbeddingModel") EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .vectorTableName("ai_chat_history")
                .dimensions(1536)
                .initializeSchema(true)
                .build();
    }
}
