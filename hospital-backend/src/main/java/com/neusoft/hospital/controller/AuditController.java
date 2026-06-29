package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.dto.OperationReportRequest;
import com.neusoft.hospital.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/audit")
public class AuditController {

    private final OperationLogService operationLogService;

    /** 前端关键操作上报（需登录） */
    @PostMapping("/report")
    public ApiResponse<Void> report(@RequestBody OperationReportRequest request) {
        operationLogService.recordFrontend(request);
        return ApiResponse.success(null, "已记录");
    }
}
