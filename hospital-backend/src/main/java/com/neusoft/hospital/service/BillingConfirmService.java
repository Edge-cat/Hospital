package com.neusoft.hospital.service;

import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;

import java.util.Map;

public interface BillingConfirmService {

    PageResult<Map<String, Object>> listPending(PageQuery query);

    Map<String, Object> getDetail(Long recordId);

    void confirm(Long recordId, Map<String, Object> body);
}
