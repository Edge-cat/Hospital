package com.neusoft.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neusoft.hospital.common.BusinessException;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.SysUser;
import com.neusoft.hospital.mapper.SysUserMapper;
import com.neusoft.hospital.service.SysUserAdminService;
import com.neusoft.hospital.service.support.EntityPageHelper;
import com.neusoft.hospital.service.support.PasswordHelper;
import com.neusoft.hospital.service.support.RoleCodes;
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
        validateCreate(entity);
        RoleCodes.applyRoleKey(entity);
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
        String rawPassword = StringUtils.hasText(entity.getPassword()) ? entity.getPassword() : "123456";
        entity.setPassword(passwordHelper.encode(rawPassword));
        mapper.insert(entity);
    }

    @Override
    public void update(Long id, SysUser entity) {
        SysUser existing = getById(id);
        entity.setId(id);
        RoleCodes.applyRoleKey(entity);
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

    private void validateCreate(SysUser entity) {
        if (!StringUtils.hasText(entity.getUsername())) {
            throw new BusinessException(400, "用户名不能为空");
        }
        if (!StringUtils.hasText(entity.getName())) {
            throw new BusinessException(400, "姓名不能为空");
        }
        Long count = mapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, entity.getUsername()));
        if (count != null && count > 0) {
            throw new BusinessException(400, "用户名已存在");
        }
    }
}
