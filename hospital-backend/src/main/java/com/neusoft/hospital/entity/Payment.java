package com.neusoft.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment")
public class Payment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String paymentNo;
    private Long patientId;
    private String patientName;
    private String itemName;
    private String itemType;
    private String department;
    private String doctorName;
    private BigDecimal amount;
    private String payMethod;
    private Integer status;
    private LocalDateTime payTime;
    private String advice;
    private String guideTip;
    private String feeBreakdown;
    private String voucherNo;
    /** 关联挂号单 id */
    private Long registerId;
    /** 关联挂号单号（与 register_id 双写，兼容历史数据） */
    private String registerNo;
    /** 关联病历 id，诊疗费缴清后解锁患者端可见 */
    private Long recordId;
    private LocalDateTime createTime;
}
