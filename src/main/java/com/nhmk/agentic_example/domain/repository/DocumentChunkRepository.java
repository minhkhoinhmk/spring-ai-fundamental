package com.nhmk.agentic_example.domain.repository;

import org.springframework.ai.document.Document;

import java.util.List;

public interface DocumentChunkRepository {
    void save(Document doc);

    void saveAll(List<Document> docs);

    List<Document> findSimilarChunks(String query, int topK);

    List<Document> findSimilarChunksWithScore(String query, int topK, double minScore);

    List<Document> findByDocumentId(String documentId);
}
