package com.neusoft.hospital.service.impl;

import com.neusoft.hospital.common.BusinessException;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.MedicalService;
import com.neusoft.hospital.mapper.MedicalServiceMapper;
import com.neusoft.hospital.service.MedicalServiceService;
import com.neusoft.hospital.service.support.EntityPageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MedicalServiceServiceImpl implements MedicalServiceService {

    private final MedicalServiceMapper mapper;

    @Override
    public PageResult<MedicalService> list(PageQuery query) {
        return EntityPageHelper.page(mapper, query, w -> {
            EntityPageHelper.keywordLike(w, query.getKeyword(), MedicalService::getServiceName);
            if (query.getStatus() != null) {
                w.eq(MedicalService::getStatus, query.getStatus());
            }
        });
    }

    @Override
    public MedicalService getById(Long id) {
        MedicalService entity = mapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "记录不存在");
        }
        return entity;
    }

    @Override
    public void create(MedicalService entity) {
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
        mapper.insert(entity);
    }

    @Override
    public void update(Long id, MedicalService entity) {
        getById(id);
        entity.setId(id);
        mapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public void toggle(Long id, Map<String, Object> body) {
        MedicalService item = getById(id);
        if (body.get("status") != null) {
            item.setStatus(Integer.parseInt(body.get("status").toString()));
            mapper.updateById(item);
        }
    }
}
