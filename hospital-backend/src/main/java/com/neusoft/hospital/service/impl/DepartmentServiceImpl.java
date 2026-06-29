package com.neusoft.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neusoft.hospital.common.BusinessException;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.SysDepartment;
import com.neusoft.hospital.mapper.SysDepartmentMapper;
import com.neusoft.hospital.service.DepartmentService;
import com.neusoft.hospital.service.support.EntityPageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final SysDepartmentMapper mapper;

    @Override
    public PageResult<SysDepartment> list(PageQuery query) {
        return EntityPageHelper.page(mapper, query, w -> {
            EntityPageHelper.keywordLike(w, query.getKeyword(), SysDepartment::getName);
            if (query.getStatus() != null) {
                w.eq(SysDepartment::getStatus, query.getStatus());
            }
        });
    }

    @Override
    public List<SysDepartment> listAll() {
        return mapper.selectList(new LambdaQueryWrapper<SysDepartment>()
                .orderByAsc(SysDepartment::getSort)
                .orderByAsc(SysDepartment::getId));
    }

    @Override
    public List<Map<String, Object>> tree() {
        return buildChildren(0L, listAll());
    }

    private List<Map<String, Object>> buildChildren(Long parentId, List<SysDepartment> all) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        for (SysDepartment dept : all) {
            long pid = dept.getParentId() != null ? dept.getParentId() : 0L;
            if (pid != parentId) {
                continue;
            }
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", dept.getId());
            node.put("name", dept.getName());
            node.put("parentId", dept.getParentId());
            node.put("sort", dept.getSort());
            node.put("status", dept.getStatus());
            node.put("description", dept.getDescription());
            node.put("children", buildChildren(dept.getId(), all));
            nodes.add(node);
        }
        return nodes;
    }

    @Override
    public SysDepartment getById(Long id) {
        SysDepartment entity = mapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "记录不存在");
        }
        return entity;
    }

    @Override
    public void create(SysDepartment entity) {
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
        mapper.insert(entity);
    }

    @Override
    public void update(Long id, SysDepartment entity) {
        getById(id);
        entity.setId(id);
        mapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
