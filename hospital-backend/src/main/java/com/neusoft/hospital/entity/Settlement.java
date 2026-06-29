package com.neusoft.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("settlement")
public class Settlement {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String settlementNo;
    private String patientName;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private Integer status;
    private LocalDateTime settleTime;
    private String itemsJson;
    private String invoiceNo;
    private LocalDateTime createTime;
}
