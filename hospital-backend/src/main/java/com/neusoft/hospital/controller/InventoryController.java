package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Inventory;
import com.neusoft.hospital.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService service;

    @GetMapping("/list")
    public ApiResponse<PageResult<Inventory>> list(PageQuery query) {
        return ApiResponse.success(service.list(query));
    }

    @PostMapping("/{id}/adjust")
    public ApiResponse<Void> adjust(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        service.adjust(id, body);
        return ApiResponse.success(null, "库存调整成功");
    }

    @PostMapping("/{id}/procurement-request")
    public ApiResponse<Map<String, Object>> procurementRequest(@PathVariable Long id,
                                                               @RequestBody(required = false) Map<String, Object> body) {
        return ApiResponse.success(service.createProcurementRequest(id, body), "采购申请单已生成");
    }

    @GetMapping("/{id}")
    public ApiResponse<Inventory> get(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Inventory entity) {
        service.create(entity);
        return ApiResponse.success(null, "添加成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Inventory entity) {
        service.update(id, entity);
        return ApiResponse.success(null, "更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null, "删除成功");
    }
}
