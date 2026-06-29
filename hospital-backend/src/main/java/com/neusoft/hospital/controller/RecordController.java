package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.MedicalRecord;
import com.neusoft.hospital.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/record")
public class RecordController {

    private final MedicalRecordService medicalRecordService;

    @GetMapping("/list")
    public ApiResponse<PageResult<MedicalRecord>> list(PageQuery query) {
        return ApiResponse.success(medicalRecordService.list(query));
    }

    @GetMapping("/{id}/chain")
    public ApiResponse<Map<String, Object>> chain(@PathVariable Long id) {
        return ApiResponse.success(medicalRecordService.getChain(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody MedicalRecord record) {
        medicalRecordService.create(record);
        return ApiResponse.success(null, "添加成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody MedicalRecord record) {
        medicalRecordService.update(id, record);
        return ApiResponse.success(null, "更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        medicalRecordService.delete(id);
        return ApiResponse.success(null, "删除成功");
    }

    @PostMapping("/{id}/revise")
    public ApiResponse<Void> revise(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        medicalRecordService.revise(id, body);
        return ApiResponse.success(null, "病历修订已提交审批");
    }

    @PostMapping("/{id}/withdraw")
    public ApiResponse<Void> withdraw(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        medicalRecordService.withdraw(id, body);
        return ApiResponse.success(null, "病历撤回申请已提交");
    }
}
