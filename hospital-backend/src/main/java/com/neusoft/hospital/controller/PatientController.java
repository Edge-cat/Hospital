package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Patient;
import com.neusoft.hospital.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/patient")
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/list")
    public ApiResponse<PageResult<Patient>> list(PageQuery query) {
        return ApiResponse.success(patientService.list(query));
    }

    @GetMapping("/search")
    public ApiResponse<PageResult<Patient>> search(PageQuery query) {
        return ApiResponse.success(patientService.search(query));
    }

    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> info() {
        return ApiResponse.success(patientService.getLoggedInPatientInfo());
    }

    @GetMapping("/{id}")
    public ApiResponse<Patient> get(@PathVariable Long id) {
        return ApiResponse.success(patientService.getById(id));
    }

    @GetMapping("/{id}/detail")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.success(patientService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<Patient> create(@RequestBody Patient patient) {
        return ApiResponse.success(patientService.create(patient), "添加成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Patient patient) {
        patientService.update(id, patient);
        return ApiResponse.success(null, "更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        patientService.delete(id);
        return ApiResponse.success(null, "删除成功");
    }

    @PostMapping("/batch-delete")
    public ApiResponse<Void> batchDelete(@RequestBody Map<String, List<Long>> body) {
        patientService.batchDelete(body.get("ids"));
        return ApiResponse.success(null, "批量删除成功");
    }

    @GetMapping("/{id}/consultation-record")
    public ApiResponse<Map<String, Object>> consultationRecord(@PathVariable Long id) {
        return ApiResponse.success(patientService.getConsultationRecord(id));
    }

    @PostMapping("/{id}/consultation")
    public ApiResponse<Void> startConsultation(@PathVariable Long id) {
        patientService.startConsultation(id);
        return ApiResponse.success(null, "已开始就诊");
    }

    @PostMapping("/{id}/finish-consultation")
    public ApiResponse<Void> finishConsultation(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        patientService.finishConsultation(id, body);
        return ApiResponse.success(null, "就诊已完成");
    }

    @PostMapping("/{id}/queue")
    public ApiResponse<Void> joinQueue(@PathVariable Long id) {
        patientService.joinQueue(id);
        return ApiResponse.success(null, "已加入候诊队列");
    }
}
