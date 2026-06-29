package com.neusoft.hospital.service;

import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Inventory;

import java.util.Map;

public interface InventoryService {

    PageResult<Inventory> list(PageQuery query);

    Inventory getById(Long id);

    void create(Inventory entity);

    void update(Long id, Inventory entity);

    void delete(Long id);

    void adjust(Long id, Map<String, Object> body);

    Map<String, Object> createProcurementRequest(Long id, Map<String, Object> body);
}
