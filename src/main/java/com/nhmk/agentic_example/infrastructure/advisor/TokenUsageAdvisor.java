package com.nhmk.agentic_example.infrastructure.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * Custom Call/Stream Advisor để theo dõi và log token usage của request và response.
 * <p>
 * Advisor này sẽ:
 * - Log số lượng token được sử dụng trong prompt (request)
 * - Log số lượng token được sinh ra trong completion (response)
 * - Tính tổng token usage cho mỗi request
 * <p>
 * Implement CallAdvisor và StreamAdvisor để có thể intercept cả regular call và streaming call.
 */
@Slf4j
public class TokenUsageAdvisor implements CallAdvisor, StreamAdvisor {
    private static final String ADVISOR_NAME = "TokenUsageAdvisor";
    private final int order;

    public TokenUsageAdvisor() {
        // Default order cao hơn để chạy sau các advisor khác (như memory advisor)
        this.order = 100;
    }

    public TokenUsageAdvisor(int order) {
        this.order = order;
    }

    @Override
    public String getName() {
        return ADVISOR_NAME;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    /**
     * Xử lý regular (non-streaming) call.
     * Log request trước khi gọi, log response và token usage sau khi nhận response.
     */
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        // Log thông tin request
        logRequestInfo(request);

        // Gọi next advisor trong chain
        ChatClientResponse response = chain.nextCall(request);

        // Log thông tin response và token usage
        logResponseInfo(response);

        return response;
    }

    /**
     * Xử lý streaming call.
     * Log request trước khi gọi, log response khi streaming hoàn thành.
     */
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        // Log thông tin request
        logRequestInfo(request);

        // Gọi next advisor trong chain và log response chunks
        return chain.nextStream(request)
                .doOnNext(this::logResponseInfo)
                .doOnError(error -> log.error("Error during streaming: {}", error.getMessage()));
    }

    /**
     * Log thông tin về request trước khi gửi đến AI model.
     */
    private void logRequestInfo(ChatClientRequest request) {
        log.info("=== AI Request ===");

        // Log prompt information
        if (request.prompt() != null) {
            log.info("Prompt: {}", truncate(request.prompt().toString(), 150));
        }

        // Log context params nếu có
        if (request.context() != null && !request.context().isEmpty()) {
            log.info("Context params: {}", request.context());
        }
    }

    /**
     * Log thông tin về response và token usage sau khi nhận từ AI model.
     */
    private void logResponseInfo(ChatClientResponse clientResponse) {
        ChatResponse response = clientResponse.chatResponse();

        log.info("=== AI Response ===");

        // Log metadata nếu có
        if (response.getMetadata() != null) {
            var usage = response.getMetadata().getUsage();

                if (usage != null) {
                    // Log chi tiết token usage - đây là phần quan trọng nhất
                    log.info("Token Usage:");
                    log.info("  - Prompt Tokens (Input): {}", usage.getPromptTokens());
                    log.info("  - Completion Tokens (Output): {}", usage.getCompletionTokens());
                    log.info("  - Total Tokens: {}", usage.getTotalTokens());
                } else {
                    log.info("Token usage information not available in response metadata");
                }

            // Log model information
            String model = response.getMetadata().getModel();
            if (model != null) {
                log.info("Model used: {}", model);
            }
        }

        // Log response content (truncated)
        String content = response.getResult() != null
                && response.getResult().getOutput() != null
                ? response.getResult().getOutput().getText()
                : "N/A";
        log.info("Response: {}", truncate(content, 100));
        log.info("==================");
    }

    /**
     * Helper method để truncate long strings cho logging.
     */
    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "null";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "... (truncated)";
    }
}
