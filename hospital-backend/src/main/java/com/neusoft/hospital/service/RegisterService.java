package com.neusoft.hospital.service;

import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.RegisterOrder;

import java.util.Map;

public interface RegisterService {

    PageResult<RegisterOrder> list(PageQuery query);

    RegisterOrder create(Map<String, Object> body);

    void cancel(Long id);
}
