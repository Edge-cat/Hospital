package com.neusoft.hospital.service;

import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.IncomeExpense;

import java.util.Map;

public interface IncomeExpenseService {

    PageResult<IncomeExpense> list(PageQuery query);

    IncomeExpense getById(Long id);

    void create(IncomeExpense entity);

    void createFromMap(Map<String, Object> body);

    Map<String, Object> summary(String startDate, String endDate);

    Map<String, Object> trace(Long id);

    void update(Long id, IncomeExpense entity);

    void delete(Long id);
}
