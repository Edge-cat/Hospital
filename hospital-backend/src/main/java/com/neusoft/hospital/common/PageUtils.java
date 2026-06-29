package com.neusoft.hospital.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public final class PageUtils {

    private PageUtils() {
    }

    public static <T> Page<T> buildPage(PageQuery query) {
        return new Page<>(query.pageOrDefault(), query.pageSizeOrDefault());
    }

    public static <T> PageResult<T> toResult(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }
}
