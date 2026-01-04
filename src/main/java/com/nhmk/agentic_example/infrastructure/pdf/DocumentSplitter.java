package com.nhmk.agentic_example.infrastructure.pdf;

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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tách documents (PDF, DOCX, TXT, etc.) thành các chunk để embedding.
 * Dùng Apache Tika để parse nhiều loại file và Spring AI TextSplitter để chunk.
 */
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
        try {
            // Dùng Tika AutoDetectParser để tự động phát hiện file format
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(-1); // -1 = unlimited
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();
            
            parser.parse(inputStream, handler, metadata, context);
            String text = handler.toString();
            
            // Dùng Spring AI TextSplitter để chia text thành chunks
            Document doc = new Document(text);
            List<Document> chunks = textSplitter.split(doc);
            
            return chunks.stream()
                    .map(Document::getText)
                    .collect(Collectors.toList());
        } catch (SAXException | TikaException e) {
            throw new IOException("Failed to parse document with Tika", e);
        }
    }
}
