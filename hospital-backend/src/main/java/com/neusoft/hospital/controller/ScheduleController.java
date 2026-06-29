package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Schedule;
import com.neusoft.hospital.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final ScheduleService service;

    @GetMapping("/list")
    public ApiResponse<PageResult<Schedule>> list(PageQuery query) {
        return ApiResponse.success(service.list(query));
    }

    @GetMapping("/calendar")
    public ApiResponse<Map<String, Object>> calendar(PageQuery query) {
        return ApiResponse.success(service.calendar(query));
    }

    @GetMapping("/{id}/affected")
    public ApiResponse<Map<String, Object>> affected(@PathVariable Long id) {
        return ApiResponse.success(service.getAffected(id));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        service.cancel(id, body);
        boolean refund = body != null && "refund".equals(body.get("action"));
        return ApiResponse.success(null, refund ? "已取消排班并处理退号" : "已取消排班并发送通知");
    }

    @GetMapping("/{id}")
    public ApiResponse<Schedule> get(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Schedule entity) {
        service.create(entity);
        return ApiResponse.success(null, "添加成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Schedule entity) {
        service.update(id, entity);
        return ApiResponse.success(null, "更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null, "删除成功");
    }
}
