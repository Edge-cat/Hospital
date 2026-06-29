package com.neusoft.hospital.service;

import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.SysLoginLog;
import com.neusoft.hospital.entity.SysOperationLog;

public interface SysLogService {

    PageResult<SysOperationLog> operationLogs(PageQuery query);

    PageResult<SysLoginLog> loginLogs(PageQuery query);
}
