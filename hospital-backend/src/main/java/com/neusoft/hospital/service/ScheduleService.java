package com.neusoft.hospital.service;

import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Schedule;

import java.util.Map;

public interface ScheduleService {

    PageResult<Schedule> list(PageQuery query);

    Schedule getById(Long id);

    void create(Schedule entity);

    void update(Long id, Schedule entity);

    void delete(Long id);

    Map<String, Object> calendar(PageQuery query);

    Map<String, Object> getAffected(Long id);

    void cancel(Long id, Map<String, Object> body);
}
