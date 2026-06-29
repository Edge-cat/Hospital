package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.IncomeExpense;
import com.neusoft.hospital.service.IncomeExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/income-expense")
public class IncomeExpenseController {

    private final IncomeExpenseService service;

    @GetMapping("/list")
    public ApiResponse<PageResult<IncomeExpense>> list(PageQuery query) {
        return ApiResponse.success(service.list(query));
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ApiResponse.success(service.summary(startDate, endDate));
    }

    @GetMapping("/{id}/trace")
    public ApiResponse<Map<String, Object>> trace(@PathVariable Long id) {
        return ApiResponse.success(service.trace(id));
    }

    @GetMapping("/{id}")
    public ApiResponse<IncomeExpense> get(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Map<String, Object> body) {
        service.createFromMap(body);
        return ApiResponse.success(null, "记录成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody IncomeExpense entity) {
        service.update(id, entity);
        return ApiResponse.success(null, "更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null, "删除成功");
    }
}
