package com.neusoft.hospital.service;

import com.neusoft.hospital.dto.OperationReportRequest;

import java.util.Map;

public interface OperationLogService {

    void recordBackend(String module, String action, int status);

    void recordFrontend(OperationReportRequest request);

    /** 患者端关键业务（小程序/用户 Web）服务端落库 */
    void recordPatientAction(String client, String module, String action, String path, String detail);

    Map<String, Object> consoleOverview();

    /** 轮询增量日志（id 升序） */
    java.util.List<com.neusoft.hospital.entity.SysOperationLog> listSince(Long sinceId, int limit);
}
