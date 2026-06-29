package com.neusoft.hospital.service;

import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Notice;

public interface NoticeService {

    PageResult<Notice> list(PageQuery query);

    Notice getById(Long id);

    void create(Notice entity);

    void update(Long id, Notice entity);

    void delete(Long id);
}
