package com.neusoft.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neusoft.hospital.common.BusinessException;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Appointment;
import com.neusoft.hospital.entity.Schedule;
import com.neusoft.hospital.mapper.AppointmentMapper;
import com.neusoft.hospital.mapper.ScheduleMapper;
import com.neusoft.hospital.service.AppointmentService;
import com.neusoft.hospital.service.support.AppointmentScheduleHelper;
import com.neusoft.hospital.service.support.EntityPageHelper;
import com.neusoft.hospital.service.support.SysConfigHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentMapper appointmentMapper;
    private final ScheduleMapper scheduleMapper;
    private final SysConfigHelper configHelper;

    @Override
    public PageResult<Appointment> list(PageQuery query) {
        return EntityPageHelper.page(appointmentMapper, query, w -> {
            EntityPageHelper.keywordLike(w, query.getKeyword(), Appointment::getPatientName, Appointment::getAppointmentNo);
            if (StringUtils.hasText(query.getDepartment())) {
                w.eq(Appointment::getDepartment, query.getDepartment());
            }
            if (query.getStatus() != null) {
                w.eq(Appointment::getStatus, query.getStatus());
            }
        });
    }

    @Override
    public Map<String, Object> buildSchedule(Long doctorId) {
        long id = doctorId != null ? doctorId : 1L;
        int days = configHelper.getInt("appointment_days", 7);
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(days - 1L);
        Set<LocalDate> scheduledDates = scheduleMapper.selectList(new LambdaQueryWrapper<Schedule>()
                        .eq(Schedule::getDoctorId, id)
                        .eq(Schedule::getStatus, 1)
                        .ge(Schedule::getShiftDate, start)
                        .le(Schedule::getShiftDate, end))
                .stream()
                .map(Schedule::getShiftDate)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, Long> bookedCounts = new HashMap<>();
        for (Appointment appointment : appointmentMapper.selectList(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getDoctorId, id)
                .ge(Appointment::getAppointmentDate, start)
                .le(Appointment::getAppointmentDate, end)
                .ne(Appointment::getStatus, 3))) {
            String key = appointment.getAppointmentDate() + "|" + appointment.getTimeSlot();
            bookedCounts.merge(key, 1L, Long::sum);
        }
        return Map.of("dates", AppointmentScheduleHelper.buildDoctorSchedule(id, days, scheduledDates, bookedCounts));
    }

    @Override
    public Appointment create(Map<String, Object> body) {
        Appointment appointment = new Appointment();
        appointment.setAppointmentNo("YY" + System.currentTimeMillis());
        appointment.setPatientName((String) body.get("patientName"));
        appointment.setDepartment((String) body.get("department"));
        appointment.setDoctorName((String) body.get("doctorName"));
        if (body.get("doctorId") != null) {
            appointment.setDoctorId(Long.valueOf(body.get("doctorId").toString()));
        }
        if (body.get("patientId") != null) {
            appointment.setPatientId(Long.valueOf(body.get("patientId").toString()));
        }
        if (body.get("appointmentDate") != null) {
            appointment.setAppointmentDate(LocalDate.parse(body.get("appointmentDate").toString()));
        }
        appointment.setTimeSlot((String) body.get("timeSlot"));
        appointment.setStatus(0);
        appointment.setCreateTime(LocalDateTime.now());
        appointmentMapper.insert(appointment);
        return appointment;
    }

    @Override
    public void confirm(Long id) {
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new BusinessException(404, "预约不存在");
        }
        appointment.setStatus(1);
        appointmentMapper.updateById(appointment);
    }

    @Override
    public void cancel(Long id) {
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new BusinessException(404, "预约不存在");
        }
        appointment.setStatus(3);
        appointmentMapper.updateById(appointment);
    }
}
