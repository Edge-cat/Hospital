package com.neusoft.hospital.service;

import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.SysDepartment;

import java.util.List;
import java.util.Map;

public interface DepartmentService {

    PageResult<SysDepartment> list(PageQuery query);

    List<SysDepartment> listAll();

    List<Map<String, Object>> tree();

    SysDepartment getById(Long id);

    void create(SysDepartment entity);

    void update(Long id, SysDepartment entity);

    void delete(Long id);
}
