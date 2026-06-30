package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.SysLoginLog;
import com.neusoft.hospital.entity.SysOperationLog;
import com.neusoft.hospital.service.OperationLogService;
import com.neusoft.hospital.service.SysLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/log")
public class SysLogController {

    private final SysLogService sysLogService;
    private final OperationLogService operationLogService;

    @GetMapping("/operation")
    public ApiResponse<PageResult<SysOperationLog>> operation(PageQuery query) {
        return ApiResponse.success(sysLogService.operationLogs(query));
    }

    /** 增量拉取操作日志（管理端轮询） */
    @GetMapping("/operation/recent")
    public ApiResponse<Map<String, Object>> operationRecent(
            @RequestParam(defaultValue = "0") Long sinceId,
            @RequestParam(defaultValue = "20") Integer limit) {
        return ApiResponse.success(Map.of("list", operationLogService.listSince(sinceId, limit)));
    }

    @GetMapping("/login")
    public ApiResponse<PageResult<SysLoginLog>> login(PageQuery query) {
        return ApiResponse.success(sysLogService.loginLogs(query));
    }
}
