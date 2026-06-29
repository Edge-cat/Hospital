package com.neusoft.hospital.service;

import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.MedicalRecord;

import java.util.Map;

public interface MedicalRecordService {

    PageResult<MedicalRecord> list(PageQuery query);

    MedicalRecord getById(Long id);

    void create(MedicalRecord record);

    void update(Long id, MedicalRecord record);

    void delete(Long id);

    Map<String, Object> getChain(Long id);

    void revise(Long id, Map<String, Object> body);

    void withdraw(Long id, Map<String, Object> body);
}
