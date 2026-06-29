package com.neusoft.hospital.service;

import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.SysRole;

public interface SysRoleService {

    PageResult<SysRole> list(PageQuery query);

    SysRole getById(Long id);

    void create(SysRole entity);

    void update(Long id, SysRole entity);

    void delete(Long id);
}
