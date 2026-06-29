package com.neusoft.hospital.service.impl;

import com.neusoft.hospital.common.BusinessException;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Doctor;
import com.neusoft.hospital.mapper.DoctorMapper;
import com.neusoft.hospital.service.DoctorService;
import com.neusoft.hospital.service.support.EntityPageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorMapper mapper;

    @Override
    public PageResult<Doctor> list(PageQuery query) {
        return EntityPageHelper.page(mapper, query, w -> {
            EntityPageHelper.keywordLike(w, query.getKeyword(), Doctor::getName, Doctor::getDepartment);
            if (StringUtils.hasText(query.getDepartment())) {
                w.eq(Doctor::getDepartment, query.getDepartment());
            }
            if (query.getStatus() != null) {
                w.eq(Doctor::getStatus, query.getStatus());
            }
        });
    }

    @Override
    public Doctor getById(Long id) {
        Doctor entity = mapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "记录不存在");
        }
        return entity;
    }

    @Override
    public void create(Doctor entity) {
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
        mapper.insert(entity);
    }

    @Override
    public void update(Long id, Doctor entity) {
        getById(id);
        entity.setId(id);
        mapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public int batchImport(List<Map<String, Object>> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (Map<String, Object> item : list) {
            Doctor doctor = new Doctor();
            doctor.setDoctorNo("D" + String.valueOf(System.currentTimeMillis()).substring(7) + count);
            doctor.setName(str(item, "name", "姓名"));
            Object gender = item.get("gender");
            if (gender != null) {
                doctor.setGender(Integer.parseInt(gender.toString()));
            } else {
                doctor.setGender("女".equals(str(item, "gender", "性别")) ? 2 : 1);
            }
            doctor.setDepartment(str(item, "department", "科室", "内科"));
            doctor.setTitle(str(item, "title", "职称", "住院医师"));
            doctor.setSpecialty(str(item, "specialty", "专业方向", "普外"));
            doctor.setPhone(str(item, "phone", "联系电话", ""));
            doctor.setStatus(1);
            doctor.setCreateTime(LocalDateTime.now());
            mapper.insert(doctor);
            count++;
        }
        return count;
    }

    private String str(Map<String, Object> item, String key, String altKey, String defaultVal) {
        Object val = item.get(key);
        if (val == null || val.toString().isBlank()) {
            val = item.get(altKey);
        }
        return val != null && !val.toString().isBlank() ? val.toString() : defaultVal;
    }

    private String str(Map<String, Object> item, String key, String altKey) {
        return str(item, key, altKey, "");
    }
}
