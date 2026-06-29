package com.neusoft.hospital.service;

import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.SysDict;

public interface SysDictService {

    PageResult<SysDict> list(PageQuery query);

    SysDict getById(Long id);

    void create(SysDict entity);

    void update(Long id, SysDict entity);

    void delete(Long id);
}
