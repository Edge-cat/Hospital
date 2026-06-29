package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.SysDepartment;
import com.neusoft.hospital.service.CommonMetaService;
import com.neusoft.hospital.service.DepartmentService;
import com.neusoft.hospital.service.StatisticsService;
import com.neusoft.hospital.service.support.RegisterConfigHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/common")
public class CommonController {

    private final CommonMetaService commonMetaService;
    private final DepartmentService departmentService;
    private final StatisticsService statisticsService;
    private final RegisterConfigHelper registerConfigHelper;

    @GetMapping("/meta")
    public ApiResponse<Map<String, Object>> meta() {
        return ApiResponse.success(commonMetaService.meta());
    }

    @GetMapping("/options/{key}")
    public ApiResponse<List<Map<String, Object>>> options(@PathVariable String key) {
        return ApiResponse.success(commonMetaService.options(key));
    }

    @GetMapping("/enums/{key}")
    public ApiResponse<Map<String, Object>> enums(@PathVariable String key) {
        return ApiResponse.success(commonMetaService.enums(key));
    }

    /** 患者端/小程序：科室列表（无需管理端权限） */
    @GetMapping("/departments")
    public ApiResponse<PageResult<SysDepartment>> departments() {
        List<SysDepartment> list = departmentService.listAll();
        return ApiResponse.success(new PageResult<>(list, list.size(), 1, list.size()));
    }

    /** 患者端首页：公开统计摘要（不含财务敏感字段） */
    @GetMapping("/home-overview")
    public ApiResponse<Map<String, Object>> homeOverview() {
        return ApiResponse.success(statisticsService.homeOverview());
    }

    /** 患者端/小程序：挂号号别与费用（读 sys_config，与管理端配置一致） */
    @GetMapping("/register-types")
    public ApiResponse<List<Map<String, Object>>> registerTypes() {
        return ApiResponse.success(registerConfigHelper.listTypes());
    }
}
