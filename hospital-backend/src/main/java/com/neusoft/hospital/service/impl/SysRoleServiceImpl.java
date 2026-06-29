package com.neusoft.hospital.service.impl;

import com.neusoft.hospital.common.BusinessException;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.SysRole;
import com.neusoft.hospital.mapper.SysRoleMapper;
import com.neusoft.hospital.service.SysRoleService;
import com.neusoft.hospital.service.support.EntityPageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper mapper;

    @Override
    public PageResult<SysRole> list(PageQuery query) {
        return EntityPageHelper.page(mapper, query, w -> {
            EntityPageHelper.keywordLike(w, query.getKeyword(), SysRole::getName);
            if (query.getStatus() != null) {
                w.eq(SysRole::getStatus, query.getStatus());
            }
        });
    }

    @Override
    public SysRole getById(Long id) {
        SysRole entity = mapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "记录不存在");
        }
        return entity;
    }

    @Override
    public void create(SysRole entity) {
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
        mapper.insert(entity);
    }

    @Override
    public void update(Long id, SysRole entity) {
        getById(id);
        entity.setId(id);
        mapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
