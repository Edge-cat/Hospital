package com.neusoft.hospital.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.neusoft.hospital.service.support.SourceModuleLabels;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("income_expense")
public class IncomeExpense {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String recordNo;
    private String recordType;
    private String category;
    private BigDecimal amount;
    private String department;
    private String operator;
    private Long accountId;
    @JsonIgnore
    private String sourceModule;
    private String sourceId;
    private LocalDateTime recordTime;
    private String remark;
    private LocalDateTime createTime;

    @JsonProperty("type")
    public String getType() {
        if (recordType == null) {
            return null;
        }
        if ("income".equalsIgnoreCase(recordType) || "收入".equals(recordType)) {
            return "收入";
        }
        if ("expense".equalsIgnoreCase(recordType) || "支出".equals(recordType)) {
            return "支出";
        }
        return recordType;
    }

    @JsonProperty("recordDate")
    public String getRecordDate() {
        return recordTime != null ? recordTime.toLocalDate().toString() : null;
    }

    @JsonProperty("sourceModule")
    public String getSourceModuleLabel() {
        return SourceModuleLabels.label(sourceModule);
    }

    @JsonIgnore
    public String getSourceModuleCode() {
        return sourceModule;
    }

    @JsonProperty("autoCollected")
    public Integer getAutoCollected() {
        return 0;
    }
}
