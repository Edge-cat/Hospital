package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Doctor;
import com.neusoft.hospital.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/doctor")
public class DoctorController {

    private final DoctorService service;

    @GetMapping("/list")
    public ApiResponse<PageResult<Doctor>> list(PageQuery query) {
        return ApiResponse.success(service.list(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<Doctor> get(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Doctor entity) {
        service.create(entity);
        return ApiResponse.success(null, "添加成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Doctor entity) {
        service.update(id, entity);
        return ApiResponse.success(null, "更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null, "删除成功");
    }

    @PostMapping("/batch-import")
    public ApiResponse<Void> batchImport(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) body.get("list");
        int count = service.batchImport(list);
        return ApiResponse.success(null, "成功导入 " + count + " 条");
    }
}
