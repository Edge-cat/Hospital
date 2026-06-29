package com.neusoft.hospital.service;

import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;

import java.util.Map;

public interface PaymentService {

    PageResult<Map<String, Object>> list(PageQuery query);

    Map<String, Object> getDetail(Long id);

    Map<String, Object> pay(Map<String, Object> body);

    Map<String, Object> batchPay(Map<String, Object> body);

    void refund(Long id);

    Map<String, Object> summary();
}
