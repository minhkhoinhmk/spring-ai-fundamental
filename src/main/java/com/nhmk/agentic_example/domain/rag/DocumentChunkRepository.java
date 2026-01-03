package com.nhmk.agentic_example.domain.rag;

import java.util.List;

/**
 * Repository interface cho lưu trữ và truy vấn DocumentChunk (vector embedding)
 */
import org.springframework.ai.document.Document;

public interface DocumentChunkRepository {
    void save(Document doc);
    void saveAll(List<Document> docs);
    List<Document> findSimilarChunks(String query, int topK);
    List<Document> findSimilarChunksWithScore(String query, int topK, double minScore);
    List<Document> findByDocumentId(String documentId);
}
