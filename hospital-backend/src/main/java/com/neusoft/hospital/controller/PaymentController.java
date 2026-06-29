package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary() {
        return ApiResponse.success(paymentService.summary());
    }

    @GetMapping("/list")
    public ApiResponse<PageResult<Map<String, Object>>> list(PageQuery query) {
        return ApiResponse.success(paymentService.list(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> get(@PathVariable Long id) {
        return ApiResponse.success(paymentService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> pay(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(paymentService.pay(body), "缴费成功");
    }

    @PostMapping("/batch")
    public ApiResponse<Map<String, Object>> batchPay(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(paymentService.batchPay(body), "批量支付成功");
    }

    @PostMapping("/{id}/refund")
    public ApiResponse<Void> refund(@PathVariable Long id) {
        paymentService.refund(id);
        return ApiResponse.success(null, "退款成功");
    }
}
