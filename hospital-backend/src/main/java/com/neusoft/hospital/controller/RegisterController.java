package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.RegisterOrder;
import com.neusoft.hospital.service.RegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/register")
public class RegisterController {

    private final RegisterService registerService;

    @GetMapping("/list")
    public ApiResponse<PageResult<RegisterOrder>> list(PageQuery query) {
        return ApiResponse.success(registerService.list(query));
    }

    @PostMapping
    public ApiResponse<RegisterOrder> create(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(registerService.create(body), "挂号成功");
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        registerService.cancel(id);
        return ApiResponse.success(null, "已退号");
    }
}
