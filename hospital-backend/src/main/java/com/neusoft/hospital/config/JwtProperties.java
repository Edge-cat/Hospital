package com.neusoft.hospital.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "hospital.jwt")
public class JwtProperties {

    private String secret = "neusoft-hospital-his-jwt-secret-key-2026-min-256-bits-long!!";
    private int expireHours = 72;
}
