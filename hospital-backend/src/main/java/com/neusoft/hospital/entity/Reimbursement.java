package com.neusoft.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("reimbursement")
public class Reimbursement {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String applyNo;
    private String applicant;
    private String department;
    private BigDecimal amount;
    private String reason;
    private Integer status;
    private LocalDateTime applyTime;
    private String workflowJson;
    private String attachmentsJson;
    private LocalDateTime approveTime;
    private String approveOperator;
    private LocalDateTime createTime;
}
