package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Appointment;
import com.neusoft.hospital.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/appointment")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping("/schedule")
    public ApiResponse<Map<String, Object>> schedule(@RequestParam(required = false) Long doctorId) {
        return ApiResponse.success(appointmentService.buildSchedule(doctorId));
    }

    @GetMapping("/list")
    public ApiResponse<PageResult<Appointment>> list(PageQuery query) {
        return ApiResponse.success(appointmentService.list(query));
    }

    @PostMapping
    public ApiResponse<Appointment> create(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(appointmentService.create(body), "预约成功");
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<Void> confirm(@PathVariable Long id) {
        appointmentService.confirm(id);
        return ApiResponse.success(null, "预约已确认");
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        appointmentService.cancel(id);
        return ApiResponse.success(null, "预约已取消");
    }
}
