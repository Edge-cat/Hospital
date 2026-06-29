package com.neusoft.hospital.service;

import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Settlement;

import java.util.Map;

public interface SettlementService {

    PageResult<Settlement> list(PageQuery query);

    Settlement getById(Long id);

    void create(Settlement entity);

    void update(Long id, Settlement entity);

    void delete(Long id);

    Map<String, Object> getDetail(Long id);

    Map<String, Object> settle(Long id, Map<String, Object> body);
}
