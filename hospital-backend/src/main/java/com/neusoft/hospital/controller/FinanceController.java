package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.FinanceAccount;
import com.neusoft.hospital.service.FinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/finance")
public class FinanceController {

    private final FinanceService service;

    @GetMapping("/list")
    public ApiResponse<PageResult<FinanceAccount>> list(PageQuery query) {
        return ApiResponse.success(service.list(query));
    }

    @GetMapping("/{id}/flows")
    public ApiResponse<Map<String, Object>> flows(@PathVariable Long id) {
        return ApiResponse.success(service.getFlows(id));
    }

    @PostMapping("/{id}/freeze")
    public ApiResponse<Void> freeze(@PathVariable Long id) {
        service.freeze(id);
        return ApiResponse.success(null, "账户已冻结");
    }

    @PostMapping("/{id}/archive")
    public ApiResponse<Void> archive(@PathVariable Long id) {
        service.archive(id);
        return ApiResponse.success(null, "账户已注销归档，流水仍可审计");
    }

    @GetMapping("/{id}")
    public ApiResponse<FinanceAccount> get(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody FinanceAccount entity) {
        service.create(entity);
        return ApiResponse.success(null, "添加成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody FinanceAccount entity) {
        service.update(id, entity);
        return ApiResponse.success(null, "更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null, "删除成功");
    }
}
