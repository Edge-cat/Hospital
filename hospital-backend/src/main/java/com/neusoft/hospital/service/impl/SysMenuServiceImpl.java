package com.neusoft.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neusoft.hospital.common.BusinessException;
import com.neusoft.hospital.entity.SysMenu;
import com.neusoft.hospital.mapper.SysMenuMapper;
import com.neusoft.hospital.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl implements SysMenuService {

    private final SysMenuMapper menuMapper;

    @Override
    public List<Map<String, Object>> tree() {
        List<SysMenu> all = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSort)
                .orderByAsc(SysMenu::getId));
        return buildChildren(0L, all);
    }

    private List<Map<String, Object>> buildChildren(Long parentId, List<SysMenu> all) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        for (SysMenu menu : all) {
            long pid = menu.getParentId() != null ? menu.getParentId() : 0L;
            if (pid != parentId) {
                continue;
            }
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", menu.getId());
            node.put("name", menu.getName());
            node.put("path", menu.getPath());
            node.put("icon", menu.getIcon());
            node.put("sort", menu.getSort());
            node.put("status", menu.getStatus());
            node.put("parentId", menu.getParentId());
            List<Map<String, Object>> children = buildChildren(menu.getId(), all);
            node.put("children", children);
            nodes.add(node);
        }
        return nodes;
    }

    @Override
    public void create(Map<String, Object> body) {
        SysMenu menu = mapToEntity(body, new SysMenu());
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        if (menu.getStatus() == null) {
            menu.setStatus(1);
        }
        if (menu.getSort() == null) {
            menu.setSort(0);
        }
        menu.setCreateTime(LocalDateTime.now());
        menuMapper.insert(menu);
    }

    @Override
    public void update(Long id, Map<String, Object> body) {
        SysMenu existing = menuMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "菜单不存在");
        }
        SysMenu menu = mapToEntity(body, existing);
        menu.setId(id);
        menuMapper.updateById(menu);
    }

    @Override
    public void delete(Long id) {
        long childCount = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException(409, "存在子菜单，无法删除");
        }
        menuMapper.deleteById(id);
    }

    private SysMenu mapToEntity(Map<String, Object> body, SysMenu menu) {
        if (body.get("name") != null) {
            menu.setName(body.get("name").toString());
        }
        if (body.get("path") != null) {
            menu.setPath(body.get("path").toString());
        }
        if (body.get("icon") != null) {
            menu.setIcon(body.get("icon").toString());
        }
        if (body.get("sort") != null) {
            menu.setSort(Integer.valueOf(body.get("sort").toString()));
        }
        if (body.get("status") != null) {
            menu.setStatus(Integer.valueOf(body.get("status").toString()));
        }
        if (body.get("parentId") != null) {
            menu.setParentId(Long.valueOf(body.get("parentId").toString()));
        }
        return menu;
    }
}
