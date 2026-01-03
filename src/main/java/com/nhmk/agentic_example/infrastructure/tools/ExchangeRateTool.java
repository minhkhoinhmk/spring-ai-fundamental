package com.nhmk.agentic_example.infrastructure.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ExchangeRateTool {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateTool.class);
    private final WebClient webClient;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public ExchangeRateTool(@Value("${exchange.rate.api-key}") String apiKey) {
        this.webClient = WebClient.builder().baseUrl("https://v6.exchangerate-api.com/v6/" + apiKey).build();
    }

    @Tool(description = "Đổi tỉ giá ngoại tệ. Nhập loại tiền nguồn, loại tiền đích và số tiền.")
    public String convertCurrency(
            @ToolParam(description = "Loại tiền nguồn (ví dụ: USD, VND, EUR, JPY, AUD, GBP, CNY, KRW, ...)") String from,
            @ToolParam(description = "Loại tiền đích (ví dụ: USD, VND, EUR, JPY, AUD, GBP, CNY, KRW, ...)") String to,
            @ToolParam(description = "Số tiền cần đổi") double amount) {
        logger.info("ExchangeRateTool convertCurrency: from={}, to={}, amount={}", from, to, amount);
        if (from == null || from.isBlank() || to == null || to.isBlank()) {
            return "Thiếu tham số loại tiền nguồn hoặc loại tiền đích. Vui lòng nhập đầy đủ!";
        }
        try {
            String url = "/latest/" + from.toUpperCase();
            String result = webClient.get().uri(url).retrieve().bodyToMono(String.class).block();
            logger.info("ExchangeRate API raw response: {}", result);
            JsonNode root = MAPPER.readTree(result);
            String resultStatus = root.path("result").asText("");
            if (!"success".equalsIgnoreCase(resultStatus)) {
                return "Không tìm thấy tỉ giá phù hợp.";
            }
            double rate = root.path("conversion_rates").path(to.toUpperCase()).asDouble(0);
            if (rate == 0) {
                return "Không tìm thấy tỉ giá phù hợp.";
            }
            double converted = amount * rate;
            return String.format("%,.2f %s = %,.2f %s (Tỉ giá: %,.4f)", amount, from.toUpperCase(), converted, to.toUpperCase(), rate);
        } catch (Exception e) {
            logger.error("ExchangeRateTool error", e);
            return "Không thể lấy tỉ giá: " + e.getMessage();
        }
    }
}
