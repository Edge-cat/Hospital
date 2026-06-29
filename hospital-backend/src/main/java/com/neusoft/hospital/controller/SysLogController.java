package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.SysLoginLog;
import com.neusoft.hospital.entity.SysOperationLog;
import com.neusoft.hospital.service.SysLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/log")
public class SysLogController {

    private final SysLogService sysLogService;

    @GetMapping("/operation")
    public ApiResponse<PageResult<SysOperationLog>> operation(PageQuery query) {
        return ApiResponse.success(sysLogService.operationLogs(query));
    }

    @GetMapping("/login")
    public ApiResponse<PageResult<SysLoginLog>> login(PageQuery query) {
        return ApiResponse.success(sysLogService.loginLogs(query));
    }
}
