package com.neusoft.hospital.common;

import lombok.Data;

@Data
public class PageQuery {

    private Integer page = 1;
    private Integer pageSize = 10;
    private String keyword;
    private String department;
    private Integer status;
    private String startDate;
    private String endDate;
    private String doctorName;
    private Integer phase;
    private String drugName;
    private String barcode;
    private String bank;
    private String dictType;
    private String module;
    private String source;
    private String client;

    public int pageOrDefault() {
        return page == null || page < 1 ? 1 : page;
    }

    public int pageSizeOrDefault() {
        return pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
    }
}
