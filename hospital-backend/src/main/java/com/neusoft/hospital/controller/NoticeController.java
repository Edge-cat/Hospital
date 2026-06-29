package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Notice;
import com.neusoft.hospital.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notice")
public class NoticeController {

    private final NoticeService service;

    @GetMapping("/list")
    public ApiResponse<PageResult<Notice>> list(PageQuery query) {
        return ApiResponse.success(service.list(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<Notice> get(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Notice entity) {
        service.create(entity);
        return ApiResponse.success(null, "添加成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Notice entity) {
        service.update(id, entity);
        return ApiResponse.success(null, "更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null, "删除成功");
    }
}
