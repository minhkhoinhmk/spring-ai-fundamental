package com.nhmk.agentic_example.application.rag;

import java.util.List;

public interface PdfIngestService {
    void ingest(String documentId, List<String> chunks);
}
