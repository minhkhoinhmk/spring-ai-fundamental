package com.nhmk.agentic_example.application.chatbot;

import com.nhmk.agentic_example.domain.chatbot.InstructionBuilder;
import com.nhmk.agentic_example.domain.chatbot.InstructionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
public class StdChatService implements ChatService {
    private final ChatClient openAIChatClient;
    private final ChatClient googleAIChatClient;
    private final InstructionBuilder instructionBuilder;
    private final String systemPrompt;

    public StdChatService(@Qualifier("openAiChatClient") ChatClient openAIChatClient,
                          @Qualifier("geminiChatClient") ChatClient googleAIChatClient,
                          InstructionBuilder instructionBuilder,
                          org.springframework.core.io.ResourceLoader resourceLoader,
                          @Value("${app.ai.system-prompt-path:prompts/system.st}") String systemPromptPath) {
        this.openAIChatClient = openAIChatClient;
        this.googleAIChatClient = googleAIChatClient;
        this.instructionBuilder = instructionBuilder;
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
     * QuestionAnswerAdvisor (wired in GoogleChatClientConfig) sẽ tự động:
     * - Query documentVectorStore với question
     * - Inject retrieved context vào prompt template
     */
    public String chatWithRag(String question) {
        return googleAIChatClient.prompt()
                .user(question)
                .call()
                .content();
    }
}
