package com.nhmk.agentic_example.application.chatbot;

import com.nhmk.agentic_example.domain.chatbot.InstructionBuilder;
import com.nhmk.agentic_example.domain.chatbot.InstructionContext;
import com.nhmk.agentic_example.domain.rag.DocumentChunkRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StdChatService implements ChatService {
    private final ChatClient openAIChatClient;
    private final ChatClient googleAIChatClient;
    private final InstructionBuilder instructionBuilder;
    private final DocumentChunkRepository documentChunkRepository;
    private final String systemPrompt;
    private final int topK;

    public StdChatService(@Qualifier("openAiChatClient") ChatClient openAIChatClient,
                          @Qualifier("geminiChatClient") ChatClient googleAIChatClient,
                          InstructionBuilder instructionBuilder,
                          DocumentChunkRepository documentChunkRepository,
                          org.springframework.core.io.ResourceLoader resourceLoader,
                          @Value("${app.ai.system-prompt-path:prompts/system.st}") String systemPromptPath,
                          @Value("${agentic_example.application.rag.top-k}") int topK) {
        this.openAIChatClient = openAIChatClient;
        this.googleAIChatClient = googleAIChatClient;
        this.instructionBuilder = instructionBuilder;
        this.documentChunkRepository = documentChunkRepository;
        this.topK = topK;
        this.systemPrompt = resolveSystemPrompt(resourceLoader, systemPromptPath);
    }

    private String resolveSystemPrompt(org.springframework.core.io.ResourceLoader resourceLoader, String systemPromptPath) {
        // Try classpath first
        try {
            String cp = systemPromptPath.startsWith("classpath:") ? systemPromptPath : "classpath:" + systemPromptPath;
            var resource = resourceLoader.getResource(cp);
            if (resource.exists()) {
                try (var in = resource.getInputStream()) {
                    String content = new String(in.readAllBytes());
                    if (!content.isBlank()) return content.trim();
                }
            }
        } catch (Exception e) {
            log.info("classpath lookup failed for {}: {}", systemPromptPath, e.getMessage());
        }

        // Then try file path
        try {
            String fp = systemPromptPath.startsWith("file:") ? systemPromptPath : "file:" + systemPromptPath;
            var resource = resourceLoader.getResource(fp);
            if (resource.exists()) {
                try (var in = resource.getInputStream()) {
                    String content = new String(in.readAllBytes());
                    if (!content.isBlank()) return content.trim();
                }
            }
        } catch (Exception e) {
            log.info("file lookup failed for {}: {}", systemPromptPath, e.getMessage());
        }

        log.info("System prompt not found at '{}' and no inline prompt configured; continuing with empty prompt.", systemPromptPath);
        return "";
    }

    @Override
    public String chat(String userPrompt, String conversationId, String detailLevel, String intent, String tone) {
        InstructionContext ctx = new InstructionContext(
                detailLevel,
                intent,
                tone,
                null,
                false,
                null
        );

        String prompt = systemPrompt + "\n" + instructionBuilder.build(ctx);

        return openAIChatClient.prompt()
                .system(prompt)
                .user(userPrompt)
                .advisors(advisor -> advisor
                        .param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    @Override
    public Flux<String> chatStream(String userPrompt, String conversationId, String detailLevel, String intent, String tone) {
        InstructionContext ctx = new InstructionContext(
                detailLevel,
                intent,
                tone,
                null,
                false,
                null
        );

        String prompt = systemPrompt + "\n" + instructionBuilder.build(ctx);

        return openAIChatClient.prompt()
                .system(prompt)
                .user(userPrompt)
                .advisors(advisor -> advisor
                        .param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }

    /**
     * Trả lời câu hỏi dựa trên context từ vector store (RAG)
     */
    public String chatWithRag(String question) {
        List<Document> contextDocs = documentChunkRepository.findSimilarChunks(question, topK);

        String context = contextDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        String prompt = "Context:\n" + context + "\n\nQuestion: " + question + "\nAnswer:";

        return googleAIChatClient.prompt()
                .system("Bạn là trợ lý AI, hãy trả lời dựa trên context cung cấp. Nếu không có thông tin trong context, hãy trả lời rằng bạn không biết.")
                .user(prompt)
                .call()
                .content();
    }
}
