package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.config.AiProperties;
import com.neusoft.hospital.dto.AiConsultRequest;
import com.neusoft.hospital.dto.AiConsultResponse;
import com.neusoft.hospital.dto.AiDoctorAssistRequest;
import com.neusoft.hospital.service.AiConsultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiConsultController {

    private final AiConsultService aiConsultService;
    private final AiProperties aiProperties;

    /** 检查 AI 密钥是否已配置（不调用 DeepSeek） */
    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> status() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("configured", aiProperties.isConfigured());
        data.put("provider", aiProperties.getProvider());
        data.put("model", aiProperties.getModel());
        data.put("demoMode", !aiProperties.isConfigured());
        return ApiResponse.success(data);
    }

    /** 基于已解锁就诊记录的 AI 问诊（DeepSeek） */
    @PostMapping("/consult")
    public ApiResponse<AiConsultResponse> consult(@Valid @RequestBody AiConsultRequest request) {
        return ApiResponse.success(aiConsultService.consult(request));
    }

    /** 医生端临床决策支持（DeepSeek） */
    @PostMapping("/doctor-assist")
    public ApiResponse<AiConsultResponse> doctorAssist(@Valid @RequestBody AiDoctorAssistRequest request) {
        return ApiResponse.success(aiConsultService.doctorAssist(request));
    }
}
