package com.neusoft.hospital.service.impl;

import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.SysLoginLog;
import com.neusoft.hospital.entity.SysOperationLog;
import com.neusoft.hospital.mapper.SysLoginLogMapper;
import com.neusoft.hospital.mapper.SysOperationLogMapper;
import com.neusoft.hospital.service.SysLogService;
import com.neusoft.hospital.service.support.EntityPageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SysLogServiceImpl implements SysLogService {

    private final SysOperationLogMapper operationLogMapper;
    private final SysLoginLogMapper loginLogMapper;

    @Override
    public PageResult<SysOperationLog> operationLogs(PageQuery query) {
        return EntityPageHelper.page(operationLogMapper, query, w -> {
            EntityPageHelper.keywordLike(w, query.getKeyword(), SysOperationLog::getOperator, SysOperationLog::getModule, SysOperationLog::getAction);
            if (StringUtils.hasText(query.getModule())) {
                w.eq(SysOperationLog::getModule, query.getModule());
            }
            if (StringUtils.hasText(query.getSource())) {
                w.eq(SysOperationLog::getSource, query.getSource());
            }
            if (StringUtils.hasText(query.getClient())) {
                w.eq(SysOperationLog::getClient, query.getClient());
            }
        });
    }

    @Override
    public PageResult<SysLoginLog> loginLogs(PageQuery query) {
        return EntityPageHelper.page(loginLogMapper, query, w ->
                EntityPageHelper.keywordLike(w, query.getKeyword(), SysLoginLog::getUsername));
    }
}
