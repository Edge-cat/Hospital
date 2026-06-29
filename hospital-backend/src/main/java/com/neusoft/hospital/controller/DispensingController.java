package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Dispensing;
import com.neusoft.hospital.service.DispensingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dispensing")
public class DispensingController {

    private final DispensingService service;

    @GetMapping("/list")
    public ApiResponse<PageResult<Dispensing>> list(PageQuery query) {
        return ApiResponse.success(service.list(query));
    }

    @GetMapping("/scan")
    public ApiResponse<Dispensing> scan(@RequestParam String barcode) {
        return ApiResponse.success(service.scanByBarcode(barcode));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<Map<String, Object>> complete(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> result = service.complete(id, body);
        return ApiResponse.success(result, "配药完成");
    }

    @GetMapping("/{id}")
    public ApiResponse<Dispensing> get(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Dispensing entity) {
        service.create(entity);
        return ApiResponse.success(null, "配药单已创建");
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Dispensing entity) {
        service.update(id, entity);
        return ApiResponse.success(null, "更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null, "删除成功");
    }
}
