package com.neusoft.hospital.service;

import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Patient;

import java.util.List;
import java.util.Map;

public interface PatientService {

    PageResult<Patient> list(PageQuery query);

    PageResult<Patient> search(PageQuery query);

    Patient getById(Long id);

    Map<String, Object> getDetail(Long id);

    Patient create(Patient patient);

    void update(Long id, Patient patient);

    void delete(Long id);

    void batchDelete(List<Long> ids);

    void startConsultation(Long id);

    void finishConsultation(Long id, Map<String, Object> body);

    void joinQueue(Long id);

    Map<String, Object> getLoggedInPatientInfo();
}
