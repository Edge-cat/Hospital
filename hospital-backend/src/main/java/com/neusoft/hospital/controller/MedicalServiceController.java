package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.MedicalService;
import com.neusoft.hospital.service.MedicalServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/service")
public class MedicalServiceController {

    private final MedicalServiceService service;

    @GetMapping("/list")
    public ApiResponse<PageResult<MedicalService>> list(PageQuery query) {
        return ApiResponse.success(service.list(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<MedicalService> get(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody MedicalService entity) {
        service.create(entity);
        return ApiResponse.success(null, "添加成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody MedicalService entity) {
        service.update(id, entity);
        return ApiResponse.success(null, "更新成功");
    }

    @PutMapping("/{id}/toggle")
    public ApiResponse<Void> toggle(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        service.toggle(id, body);
        Integer status = body.get("status") != null ? Integer.parseInt(body.get("status").toString()) : null;
        return ApiResponse.success(null, status != null && status == 1
                ? "已启用，前台缴费系统已同步" : "已禁用");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null, "删除成功");
    }
}
