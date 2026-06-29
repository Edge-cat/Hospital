package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Drug;
import com.neusoft.hospital.service.DrugService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/drug")
public class DrugController {

    private final DrugService service;

    @GetMapping("/list")
    public ApiResponse<PageResult<Drug>> list(PageQuery query) {
        return ApiResponse.success(service.list(query));
    }

    @GetMapping("/{id}/inventory")
    public ApiResponse<Map<String, Object>> inventory(@PathVariable Long id) {
        return ApiResponse.success(service.getInventory(id));
    }

    @GetMapping("/{id}/procurement-trend")
    public ApiResponse<Map<String, Object>> procurementTrend(@PathVariable Long id) {
        return ApiResponse.success(service.getProcurementTrend(id));
    }

    @PostMapping("/{id}/archive")
    public ApiResponse<Void> archive(@PathVariable Long id) {
        service.archive(id);
        return ApiResponse.success(null, "已归档，历史处方仍可追溯");
    }

    @GetMapping("/{id}")
    public ApiResponse<Drug> get(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Drug entity) {
        service.create(entity);
        return ApiResponse.success(null, "添加成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Drug entity) {
        service.update(id, entity);
        return ApiResponse.success(null, "更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null, "删除成功");
    }
}
