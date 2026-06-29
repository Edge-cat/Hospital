package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.common.OperationLog;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.SysUser;
import com.neusoft.hospital.service.SysUserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/user")
public class SysUserAdminController {

    private final SysUserAdminService service;

    @GetMapping("/list")
    public ApiResponse<PageResult<SysUser>> list(PageQuery query) {
        return ApiResponse.success(service.list(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<SysUser> get(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    @OperationLog(module = "用户管理", action = "新增用户")
    public ApiResponse<Void> create(@RequestBody SysUser entity) {
        service.create(entity);
        return ApiResponse.success(null, "添加成功");
    }

    @PutMapping("/{id}")
    @OperationLog(module = "用户管理", action = "更新用户")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody SysUser entity) {
        service.update(id, entity);
        return ApiResponse.success(null, "更新成功");
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "用户管理", action = "删除用户")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null, "删除成功");
    }

    @PostMapping("/{id}/reset-password")
    @OperationLog(module = "用户管理", action = "重置密码")
    public ApiResponse<Void> resetPassword(@PathVariable Long id) {
        service.resetPassword(id);
        return ApiResponse.success(null, "密码已重置为123456");
    }
}
