package com.nhmk.agentic_example.application.rag;

import com.nhmk.agentic_example.domain.repository.DocumentChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StdPdfIngestService implements PdfIngestService {
    private final DocumentChunkRepository documentChunkRepository;

    /**
     * Xử lý file PDF, chia nhỏ thành các chunk, sinh embedding và lưu vào vector store
     *
     * @param documentId id tài liệu (ví dụ: tên file)
     * @param chunks     danh sách đoạn text đã tách từ PDF
     */
    @Transactional
    public void ingest(String documentId, List<String> chunks) {
        List<Document> docs = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            String chunkText = chunks.get(i);
            Map<String, Object> meta = Map.of("source", documentId, "chunkIndex", i);
            docs.add(new Document(chunkText, meta));
        }
        documentChunkRepository.saveAll(docs);
    }
}
