package com.neusoft.hospital.service;

import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Procurement;

import java.util.Map;

public interface ProcurementService {

    PageResult<Procurement> list(PageQuery query);

    Procurement getById(Long id);

    void create(Procurement entity);

    void update(Long id, Procurement entity);

    void delete(Long id);

    Map<String, Object> getLogistics(Long id);

    void advance(Long id);

    void stockIn(Long id);
}
