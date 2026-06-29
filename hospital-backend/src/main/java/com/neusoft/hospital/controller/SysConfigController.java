package com.neusoft.hospital.controller;



import com.neusoft.hospital.common.ApiResponse;

import com.neusoft.hospital.service.SysConfigService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;



import java.util.List;

import java.util.Map;



@RestController

@RequiredArgsConstructor

@RequestMapping("/api/admin/config")

public class SysConfigController {



    private final SysConfigService sysConfigService;



    @GetMapping("/list")

    public ApiResponse<List<Map<String, Object>>> list() {

        return ApiResponse.success(sysConfigService.listForView());

    }



    @PutMapping

    public ApiResponse<Void> save(@RequestBody Map<String, Object> form) {

        sysConfigService.saveByForm(form);

        return ApiResponse.success(null, "配置已保存");

    }

}

