package com.nhmk.agentic_example.infrastructure.pdf;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tách documents (PDF, DOCX, TXT, etc.) thành các chunk để embedding.
 * Dùng Apache Tika để parse nhiều loại file và Spring AI TextSplitter để chunk.
 */
@Slf4j
@Component
public class DocumentSplitter {
    private final TokenTextSplitter textSplitter;

    public DocumentSplitter() {
        // Cấu hình TextSplitter: default chunk size 800 tokens, overlap 200 tokens
        this.textSplitter = new TokenTextSplitter();
    }

    /**
     * Parse và split document thành các chunks
     * @param inputStream stream của file (PDF, DOCX, TXT, etc.)
     * @return danh sách các chunk text
     */
    public List<String> split(InputStream inputStream) throws IOException {
        // Buffer input so we can retry parsing/fallback
        byte[] data = inputStream.readAllBytes();

        // First attempt: Tika auto-detect parser
        try (InputStream is = new java.io.ByteArrayInputStream(data)) {
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(-1); // unlimited
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();

            parser.parse(is, handler, metadata, context);
            String text = handler.toString();

            Document doc = new Document(text);
            List<Document> chunks = textSplitter.split(doc);
            return chunks.stream().map(Document::getText).collect(Collectors.toList());
        } catch (SAXException | TikaException | IOException e) {
            log.info("Tika parse failed, attempting pdftotext fallback: {}", e.getMessage());
            // Fallback: try external `pdftotext` if available (better tolerance for malformed PDFs)
            String extracted = tryPdftotext(data);
            if (extracted != null && !extracted.isBlank()) {
                Document doc = new Document(extracted);
                List<Document> chunks = textSplitter.split(doc);
                return chunks.stream().map(Document::getText).collect(Collectors.toList());
            }
            throw new IOException("Failed to parse document with Tika", e);
        }
    }

    private String tryPdftotext(byte[] data) {
        try {
            Path temp = Files.createTempFile("agentic-pdf-", ".pdf");
            Files.write(temp, data);
            ProcessBuilder pb = new ProcessBuilder("pdftotext", "-layout", temp.toString(), "-");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            byte[] out = p.getInputStream().readAllBytes();
            int rc = p.waitFor();
            Files.deleteIfExists(temp);
            if (rc == 0) {
                return new String(out, java.nio.charset.StandardCharsets.UTF_8);
            } else {
                log.info("pdftotext exited with code {}", rc);
                return null;
            }
        } catch (Throwable ex) {
            log.info("pdftotext fallback not available or failed: {}", ex.getMessage());
            return null;
        }
    }
}
