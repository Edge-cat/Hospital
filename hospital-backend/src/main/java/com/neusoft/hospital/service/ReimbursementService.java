package com.neusoft.hospital.service;

import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Reimbursement;

import java.util.Map;

public interface ReimbursementService {

    PageResult<Reimbursement> list(PageQuery query);

    Reimbursement getById(Long id);

    void create(Reimbursement entity);

    void update(Long id, Reimbursement entity);

    void delete(Long id);

    Map<String, Object> getDetail(Long id);

    void approve(Long id, Map<String, Object> body);

    void reject(Long id, Map<String, Object> body);
}
