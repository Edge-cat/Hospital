package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Procurement;
import com.neusoft.hospital.service.ProcurementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/procurement")
public class ProcurementController {

    private final ProcurementService service;

    @GetMapping("/list")
    public ApiResponse<PageResult<Procurement>> list(PageQuery query) {
        return ApiResponse.success(service.list(query));
    }

    @GetMapping("/{id}/logistics")
    public ApiResponse<Map<String, Object>> logistics(@PathVariable Long id) {
        return ApiResponse.success(service.getLogistics(id));
    }

    @PostMapping("/{id}/advance")
    public ApiResponse<Void> advance(@PathVariable Long id) {
        service.advance(id);
        return ApiResponse.success(null, "状态已推进");
    }

    @PostMapping("/{id}/stock-in")
    public ApiResponse<Void> stockIn(@PathVariable Long id) {
        service.stockIn(id);
        return ApiResponse.success(null, "已入库，库存已更新");
    }

    @GetMapping("/{id}")
    public ApiResponse<Procurement> get(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Procurement entity) {
        service.create(entity);
        return ApiResponse.success(null, "采购单已创建");
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Procurement entity) {
        service.update(id, entity);
        return ApiResponse.success(null, "更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null, "删除成功");
    }
}
