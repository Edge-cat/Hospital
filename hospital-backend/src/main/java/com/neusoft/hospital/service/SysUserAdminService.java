package com.neusoft.hospital.service;

import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.SysUser;

public interface SysUserAdminService {

    PageResult<SysUser> list(PageQuery query);

    SysUser getById(Long id);

    void create(SysUser entity);

    void update(Long id, SysUser entity);

    void delete(Long id);

    void resetPassword(Long id);
}
