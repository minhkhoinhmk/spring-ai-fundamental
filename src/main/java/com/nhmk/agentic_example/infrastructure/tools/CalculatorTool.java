package com.nhmk.agentic_example.infrastructure.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class CalculatorTool {
    private static final Logger logger = LoggerFactory.getLogger(CalculatorTool.class);

    @Tool(description = "Tính tổng hai số")
    public double add(@ToolParam(description = "Số thứ nhất") double a,
                      @ToolParam(description = "Số thứ hai") double b) {
        logger.info("Calling add with a={}, b={}", a, b);
        double result = a + b;
        logger.info("Result: {}", result);
        return result;
    }

    @Tool(description = "Tính hiệu hai số")
    public double subtract(@ToolParam(description = "Số bị trừ") double a,
                           @ToolParam(description = "Số trừ") double b) {
        logger.info("Calling subtract with a={}, b={}", a, b);
        double result = a - b;
        logger.info("Result: {}", result);
        return result;
    }

    @Tool(description = "Tính tích hai số")
    public double multiply(@ToolParam(description = "Số thứ nhất") double a,
                           @ToolParam(description = "Số thứ hai") double b) {
        logger.info("Calling multiply with a={}, b={}", a, b);
        double result = a * b;
        logger.info("Result: {}", result);
        return result;
    }

    @Tool(description = "Tính thương hai số")
    public double divide(@ToolParam(description = "Số bị chia") double a,
                         @ToolParam(description = "Số chia") double b) {
        logger.info("Calling divide with a={}, b={}", a, b);
        if (b == 0) {
            logger.error("Division by zero attempted");
            throw new IllegalArgumentException("Không thể chia cho 0");
        }
        double result = a / b;
        logger.info("Result: {}", result);
        return result;
    }
}
