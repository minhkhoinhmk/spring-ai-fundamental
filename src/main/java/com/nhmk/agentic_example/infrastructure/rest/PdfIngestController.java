package com.nhmk.agentic_example.infrastructure.rest;

import com.nhmk.agentic_example.application.rag.StdPdfIngestService;
import com.nhmk.agentic_example.infrastructure.pdf.DocumentSplitter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/rag/ingest")
@RequiredArgsConstructor
public class PdfIngestController {
    private final StdPdfIngestService stdPdfIngestService;
    private final DocumentSplitter documentSplitter;

    @PostMapping
    public ResponseEntity<String> ingestDocument(@RequestParam("file") MultipartFile file) throws IOException {
        String documentId = file.getOriginalFilename();
        List<String> chunks = documentSplitter.split(file.getInputStream());
        stdPdfIngestService.ingest(documentId, chunks);
        return ResponseEntity.ok("Ingested " + documentId);
    }
}
