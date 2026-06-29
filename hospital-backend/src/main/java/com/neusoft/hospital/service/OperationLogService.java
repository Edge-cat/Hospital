package com.neusoft.hospital.service;

import com.neusoft.hospital.dto.OperationReportRequest;

import java.util.Map;

public interface OperationLogService {

    void recordBackend(String module, String action, int status);

    void recordFrontend(OperationReportRequest request);

    Map<String, Object> consoleOverview();
}
