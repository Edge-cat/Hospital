package com.neusoft.hospital.service;

import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Dispensing;

import java.util.Map;

public interface DispensingService {

    PageResult<Dispensing> list(PageQuery query);

    Dispensing getById(Long id);

    void create(Dispensing entity);

    void update(Long id, Dispensing entity);

    void delete(Long id);

    Dispensing scanByBarcode(String barcode);

    Map<String, Object> complete(Long id, Map<String, Object> body);
}
