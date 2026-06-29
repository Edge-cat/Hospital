package com.neusoft.hospital.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "hospital.cors")
public class CorsProperties {

    private String allowedOrigins = "http://localhost:5173,http://localhost:5174,http://localhost:5175";

    public List<String> originList() {
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
