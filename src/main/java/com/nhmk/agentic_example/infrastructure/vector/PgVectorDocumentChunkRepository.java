package com.nhmk.agentic_example.infrastructure.vector;

import com.nhmk.agentic_example.domain.rag.DocumentChunkRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PgVectorDocumentChunkRepository implements DocumentChunkRepository {
    private final PgVectorStore vectorStore;

    @Autowired
    public PgVectorDocumentChunkRepository(@org.springframework.beans.factory.annotation.Qualifier("documentVectorStore") PgVectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override

    public void save(Document doc) {
        saveAll(List.of(doc));
    }

    @Override
    public void saveAll(List<Document> docs) {
        vectorStore.add(docs);
    }

    @Override
    public List<Document> findSimilarChunks(String query, int topK) {
        var request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();
        return vectorStore.similaritySearch(request);
    }

    @Override
    public List<Document> findSimilarChunksWithScore(String query, int topK, double minScore) {
        var request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(minScore)
                .build();
        return vectorStore.similaritySearch(request);
    }

    @Override
    public List<Document> findByDocumentId(String documentId) {
        // VectorStore không hỗ trợ truy vấn chỉ theo metadata nếu không có query; trả về rỗng.
        return List.of();
    }
}
