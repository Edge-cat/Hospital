package com.neusoft.hospital.service;

import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Doctor;

import java.util.List;
import java.util.Map;

public interface DoctorService {

    PageResult<Doctor> list(PageQuery query);

    Doctor getById(Long id);

    void create(Doctor entity);

    void update(Long id, Doctor entity);

    void delete(Long id);

    int batchImport(List<Map<String, Object>> list);
}
