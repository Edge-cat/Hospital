package com.neusoft.hospital.service;

import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.MedicalService;

import java.util.Map;

public interface MedicalServiceService {

    PageResult<MedicalService> list(PageQuery query);

    MedicalService getById(Long id);

    void create(MedicalService entity);

    void update(Long id, MedicalService entity);

    void delete(Long id);

    void toggle(Long id, Map<String, Object> body);
}
