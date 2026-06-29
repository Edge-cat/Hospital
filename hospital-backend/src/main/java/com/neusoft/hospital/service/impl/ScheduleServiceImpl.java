package com.neusoft.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neusoft.hospital.common.BusinessException;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Appointment;
import com.neusoft.hospital.entity.Schedule;
import com.neusoft.hospital.mapper.AppointmentMapper;
import com.neusoft.hospital.mapper.ScheduleMapper;
import com.neusoft.hospital.service.ScheduleService;
import com.neusoft.hospital.service.support.EntityPageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleMapper mapper;
    private final AppointmentMapper appointmentMapper;

    @Override
    public PageResult<Schedule> list(PageQuery query) {
        return EntityPageHelper.page(mapper, query, w -> {
            EntityPageHelper.keywordLike(w, query.getKeyword(), Schedule::getDoctorName, Schedule::getDepartment);
            if (query.getStatus() != null) {
                w.eq(Schedule::getStatus, query.getStatus());
            }
        });
    }

    @Override
    public Schedule getById(Long id) {
        Schedule entity = mapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "记录不存在");
        }
        return entity;
    }

    @Override
    public void create(Schedule entity) {
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
        mapper.insert(entity);
    }

    @Override
    public void update(Long id, Schedule entity) {
        getById(id);
        entity.setId(id);
        mapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public Map<String, Object> calendar(PageQuery query) {
        LambdaQueryWrapper<Schedule> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getDepartment())) {
            w.eq(Schedule::getDepartment, query.getDepartment());
        }
        if (StringUtils.hasText(query.getDoctorName())) {
            w.eq(Schedule::getDoctorName, query.getDoctorName());
        }
        if (StringUtils.hasText(query.getStartDate()) && StringUtils.hasText(query.getEndDate())) {
            w.ge(Schedule::getShiftDate, LocalDate.parse(query.getStartDate()));
            w.le(Schedule::getShiftDate, LocalDate.parse(query.getEndDate()));
        }
        w.orderByAsc(Schedule::getShiftDate);
        List<Schedule> list = mapper.selectList(w);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        return result;
    }

    @Override
    public Map<String, Object> getAffected(Long id) {
        Schedule schedule = getById(id);
        List<Appointment> affected = appointmentMapper.selectList(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getDoctorName, schedule.getDoctorName())
                .eq(Appointment::getAppointmentDate, schedule.getShiftDate())
                .ne(Appointment::getStatus, 3)
                .last("LIMIT 3"));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", affected);
        result.put("count", affected.size());
        return result;
    }

    @Override
    @Transactional
    public void cancel(Long id, Map<String, Object> body) {
        Schedule schedule = getById(id);
        schedule.setStatus(0);
        schedule.setShiftType("停诊");
        mapper.updateById(schedule);
        if (body != null && "refund".equals(body.get("action"))) {
            List<Appointment> appointments = appointmentMapper.selectList(new LambdaQueryWrapper<Appointment>()
                    .eq(Appointment::getDoctorName, schedule.getDoctorName())
                    .eq(Appointment::getAppointmentDate, schedule.getShiftDate()));
            for (Appointment appointment : appointments) {
                appointment.setStatus(3);
                appointmentMapper.updateById(appointment);
            }
        }
    }
}
