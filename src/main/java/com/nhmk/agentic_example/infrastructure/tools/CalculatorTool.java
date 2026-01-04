package com.nhmk.agentic_example.infrastructure.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CalculatorTool {

    @Tool(description = "Tính tổng hai số")
    public double add(@ToolParam(description = "Số thứ nhất") double a,
                      @ToolParam(description = "Số thứ hai") double b) {
        log.info("Calling add with a={}, b={}", a, b);
        double result = a + b;
        log.info("Result: {}", result);
        return result;
    }

    @Tool(description = "Tính hiệu hai số")
    public double subtract(@ToolParam(description = "Số bị trừ") double a,
                           @ToolParam(description = "Số trừ") double b) {
        log.info("Calling subtract with a={}, b={}", a, b);
        double result = a - b;
        log.info("Result: {}", result);
        return result;
    }

    @Tool(description = "Tính tích hai số")
    public double multiply(@ToolParam(description = "Số thứ nhất") double a,
                           @ToolParam(description = "Số thứ hai") double b) {
        log.info("Calling multiply with a={}, b={}", a, b);
        double result = a * b;
        log.info("Result: {}", result);
        return result;
    }

    @Tool(description = "Tính thương hai số")
    public double divide(@ToolParam(description = "Số bị chia") double a,
                         @ToolParam(description = "Số chia") double b) {
        log.info("Calling divide with a={}, b={}", a, b);
        if (b == 0) {
            log.error("Division by zero attempted");
            throw new IllegalArgumentException("Không thể chia cho 0");
        }
        double result = a / b;
        log.info("Result: {}", result);
        return result;
    }
}
