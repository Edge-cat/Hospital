package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.common.OperationLog;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Settlement;
import com.neusoft.hospital.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlement")
public class SettlementController {

    private final SettlementService service;

    @GetMapping("/list")
    public ApiResponse<PageResult<Settlement>> list(PageQuery query) {
        return ApiResponse.success(service.list(query));
    }

    @GetMapping("/{id}/detail")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.success(service.getDetail(id));
    }

    @GetMapping("/{id}")
    public ApiResponse<Settlement> get(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Settlement entity) {
        service.create(entity);
        return ApiResponse.success(null, "创建成功");
    }

    @PostMapping("/{id}/settle")
    @OperationLog(module = "结算管理", action = "完成结算")
    public ApiResponse<Map<String, Object>> settle(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        return ApiResponse.success(service.settle(id, body != null ? body : Map.of()), "结算完成");
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Settlement entity) {
        service.update(id, entity);
        return ApiResponse.success(null, "更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null, "删除成功");
    }
}
