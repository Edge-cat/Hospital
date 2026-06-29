package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.SysRole;
import com.neusoft.hospital.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/role")
public class SysRoleController {

    private final SysRoleService service;

    @GetMapping("/list")
    public ApiResponse<PageResult<SysRole>> list(PageQuery query) {
        return ApiResponse.success(service.list(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<SysRole> get(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody SysRole entity) {
        service.create(entity);
        return ApiResponse.success(null, "添加成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody SysRole entity) {
        service.update(id, entity);
        return ApiResponse.success(null, "更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null, "删除成功");
    }
}
