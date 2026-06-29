package com.neusoft.hospital.service.impl;

import com.neusoft.hospital.common.BusinessException;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.SysDict;
import com.neusoft.hospital.mapper.SysDictMapper;
import com.neusoft.hospital.service.SysDictService;
import com.neusoft.hospital.service.support.EntityPageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SysDictServiceImpl implements SysDictService {

    private final SysDictMapper mapper;

    @Override
    public PageResult<SysDict> list(PageQuery query) {
        return EntityPageHelper.page(mapper, query, w -> {
            EntityPageHelper.keywordLike(w, query.getKeyword(), SysDict::getDictLabel, SysDict::getDictType);
            if (StringUtils.hasText(query.getDictType())) {
                w.eq(SysDict::getDictType, query.getDictType());
            }
            if (query.getStatus() != null) {
                w.eq(SysDict::getStatus, query.getStatus());
            }
        });
    }

    @Override
    public SysDict getById(Long id) {
        SysDict entity = mapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "记录不存在");
        }
        return entity;
    }

    @Override
    public void create(SysDict entity) {
        mapper.insert(entity);
    }

    @Override
    public void update(Long id, SysDict entity) {
        getById(id);
        entity.setId(id);
        mapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
