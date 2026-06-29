package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview() {
        return ApiResponse.success(statisticsService.overview());
    }

    @GetMapping("/analysis")
    public ApiResponse<Map<String, Object>> analysis() {
        return ApiResponse.success(statisticsService.analysis());
    }

    @GetMapping("/reports")
    public ApiResponse<Map<String, Object>> reports() {
        return ApiResponse.success(statisticsService.reports());
    }

    @GetMapping("/decision")
    public ApiResponse<Map<String, Object>> decision() {
        return ApiResponse.success(statisticsService.decision());
    }
}
