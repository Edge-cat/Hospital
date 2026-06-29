package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.common.OperationLog;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Reimbursement;
import com.neusoft.hospital.service.ReimbursementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reimbursement")
public class ReimbursementController {

    private final ReimbursementService service;

    @GetMapping("/list")
    public ApiResponse<PageResult<Reimbursement>> list(PageQuery query) {
        return ApiResponse.success(service.list(query));
    }

    @GetMapping("/{id}/detail")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.success(service.getDetail(id));
    }

    @GetMapping("/{id}")
    public ApiResponse<Reimbursement> get(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Reimbursement entity) {
        service.create(entity);
        return ApiResponse.success(null, "提交成功");
    }

    @PostMapping("/{id}/approve")
    @OperationLog(module = "报销管理", action = "审批通过")
    public ApiResponse<Void> approve(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        service.approve(id, body != null ? body : Map.of());
        return ApiResponse.success(null, "审批通过");
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<Void> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        service.reject(id, body != null ? body : Map.of());
        return ApiResponse.success(null, "已驳回");
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Reimbursement entity) {
        service.update(id, entity);
        return ApiResponse.success(null, "更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null, "删除成功");
    }
}
