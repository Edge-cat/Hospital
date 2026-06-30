package com.neusoft.hospital.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Data
@Component
@ConfigurationProperties(prefix = "hospital.ai")
public class AiProperties {

    /** 是否启用 AI 问诊（需配置 apiKey） */
    private boolean enabled = true;

    private String provider = "deepseek";

    private String apiKey = "";

    private String baseUrl = "https://api.deepseek.com";

    private String model = "deepseek-chat";

    private int maxTokens = 1200;

    private double temperature = 0.6;

    public boolean isConfigured() {
        return enabled && StringUtils.hasText(apiKey);
    }
}
