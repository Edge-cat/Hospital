package com.neusoft.hospital.service;

import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Appointment;

import java.util.Map;

public interface AppointmentService {

    PageResult<Appointment> list(PageQuery query);

    Map<String, Object> buildSchedule(Long doctorId);

    Appointment create(Map<String, Object> body);

    void confirm(Long id);

    void cancel(Long id);
}
