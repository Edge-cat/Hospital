package com.neusoft.hospital.service.impl;



import com.neusoft.hospital.common.BusinessException;

import com.neusoft.hospital.common.PageQuery;

import com.neusoft.hospital.common.PageResult;

import com.neusoft.hospital.entity.SysUser;

import com.neusoft.hospital.mapper.SysUserMapper;

import com.neusoft.hospital.service.SysUserAdminService;

import com.neusoft.hospital.service.support.EntityPageHelper;

import com.neusoft.hospital.service.support.PasswordHelper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;



import java.time.LocalDateTime;



@Service

@RequiredArgsConstructor

public class SysUserAdminServiceImpl implements SysUserAdminService {



    private final SysUserMapper mapper;

    private final PasswordHelper passwordHelper;



    @Override

    public PageResult<SysUser> list(PageQuery query) {

        return EntityPageHelper.page(mapper, query, w -> {

            EntityPageHelper.keywordLike(w, query.getKeyword(), SysUser::getUsername, SysUser::getName);

            if (query.getStatus() != null) {

                w.eq(SysUser::getStatus, query.getStatus());

            }

        });

    }



    @Override

    public SysUser getById(Long id) {

        SysUser entity = mapper.selectById(id);

        if (entity == null) {

            throw new BusinessException(404, "记录不存在");

        }

        return entity;

    }



    @Override

    public void create(SysUser entity) {

        if (entity.getCreateTime() == null) {

            entity.setCreateTime(LocalDateTime.now());

        }

        if (StringUtils.hasText(entity.getPassword())) {

            entity.setPassword(passwordHelper.encode(entity.getPassword()));

        }

        mapper.insert(entity);

    }



    @Override

    public void update(Long id, SysUser entity) {

        SysUser existing = getById(id);

        entity.setId(id);

        if (StringUtils.hasText(entity.getPassword())) {

            entity.setPassword(passwordHelper.encode(entity.getPassword()));

        } else {

            entity.setPassword(existing.getPassword());

        }

        mapper.updateById(entity);

    }



    @Override

    public void delete(Long id) {

        mapper.deleteById(id);

    }



    @Override

    public void resetPassword(Long id) {

        SysUser user = getById(id);

        user.setPassword(passwordHelper.encode("123456"));

        mapper.updateById(user);

    }

}

