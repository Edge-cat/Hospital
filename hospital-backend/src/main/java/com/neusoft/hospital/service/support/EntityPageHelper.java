package com.neusoft.hospital.service.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.common.PageUtils;
import org.springframework.util.StringUtils;

import java.util.function.Consumer;

public final class EntityPageHelper {

    private EntityPageHelper() {
    }

    public static <T> PageResult<T> page(BaseMapper<T> mapper, PageQuery query, Consumer<LambdaQueryWrapper<T>> filter) {
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
        if (filter != null) {
            filter.accept(wrapper);
        }
        wrapper.last("ORDER BY id DESC");
        Page<T> page = PageUtils.buildPage(query);
        IPage<T> result = mapper.selectPage(page, wrapper);
        return PageUtils.toResult(result);
    }

    @SafeVarargs
    public static <T> void keywordLike(LambdaQueryWrapper<T> wrapper, String keyword, SFunction<T, ?>... columns) {
        if (!StringUtils.hasText(keyword) || columns.length == 0) {
            return;
        }
        wrapper.and(w -> {
            for (int i = 0; i < columns.length; i++) {
                if (i == 0) {
                    w.like(columns[i], keyword);
                } else {
                    w.or().like(columns[i], keyword);
                }
            }
        });
    }
}
