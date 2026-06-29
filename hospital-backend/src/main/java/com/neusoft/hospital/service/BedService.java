package com.neusoft.hospital.service;

import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Bed;

public interface BedService {

    PageResult<Bed> list(PageQuery query);

    Bed getById(Long id);

    void create(Bed entity);

    void update(Long id, Bed entity);

    void delete(Long id);
}
