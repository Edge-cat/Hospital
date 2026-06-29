package com.neusoft.hospital.service;

import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.FinanceAccount;

import java.util.Map;

public interface FinanceService {

    PageResult<FinanceAccount> list(PageQuery query);

    FinanceAccount getById(Long id);

    void create(FinanceAccount entity);

    void update(Long id, FinanceAccount entity);

    void delete(Long id);

    Map<String, Object> getFlows(Long id);

    void freeze(Long id);

    void archive(Long id);
}
