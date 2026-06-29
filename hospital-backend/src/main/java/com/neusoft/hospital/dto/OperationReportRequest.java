package com.neusoft.hospital.dto;

import lombok.Data;

@Data
public class OperationReportRequest {

    private String module;
    private String action;
    private String path;
    private String detail;
    /** admin | user | mini */
    private String client;
    private Integer status;
}
