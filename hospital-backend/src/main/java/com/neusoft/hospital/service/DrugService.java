package com.neusoft.hospital.service;

import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Drug;

import java.util.Map;

public interface DrugService {

    PageResult<Drug> list(PageQuery query);

    Drug getById(Long id);

    void create(Drug entity);

    void update(Long id, Drug entity);

    void delete(Long id);

    Map<String, Object> getInventory(Long id);

    Map<String, Object> getProcurementTrend(Long id);

    void archive(Long id);
}
