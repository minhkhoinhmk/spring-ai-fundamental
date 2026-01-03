package com.nhmk.agentic_example.infrastructure.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Tách PDF thành các đoạn nhỏ (chunk) để embedding
 */
@Component
public class PdfSplitter {
    public List<String> split(InputStream pdfInputStream) throws IOException {
        List<String> chunks = new ArrayList<>();
        try (PDDocument document = PDDocument.load(pdfInputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            // Chia đoạn theo số lượng ký tự (ví dụ 1000 ký tự/chunk)
            int chunkSize = 1000;
            for (int start = 0; start < text.length(); start += chunkSize) {
                int end = Math.min(start + chunkSize, text.length());
                chunks.add(text.substring(start, end));
            }
        }
        return chunks;
    }
}
