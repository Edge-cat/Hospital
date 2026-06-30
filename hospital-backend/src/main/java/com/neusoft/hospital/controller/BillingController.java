package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.service.BillingConfirmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/billing")
public class BillingController {

    private final BillingConfirmService billingConfirmService;

    @GetMapping("/pending")
    public ApiResponse<PageResult<Map<String, Object>>> pending(PageQuery query) {
        return ApiResponse.success(billingConfirmService.listPending(query));
    }

    @GetMapping("/{recordId}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long recordId) {
        return ApiResponse.success(billingConfirmService.getDetail(recordId));
    }

    @PostMapping("/{recordId}/confirm")
    public ApiResponse<Void> confirm(@PathVariable Long recordId, @RequestBody Map<String, Object> body) {
        billingConfirmService.confirm(recordId, body);
        return ApiResponse.success(null, "计价确认成功，已生成患者待缴账单");
    }
}
