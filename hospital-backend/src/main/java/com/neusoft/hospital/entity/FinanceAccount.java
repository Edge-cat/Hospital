package com.neusoft.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("finance_account")
public class FinanceAccount {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String accountNo;
    private String accountName;
    private String accountType;
    private BigDecimal balance;
    private String bank;
    private Integer overdraft;
    private BigDecimal warnThreshold;
    private Integer archived;
    private Integer status;
    private LocalDateTime updateTime;
    private LocalDateTime createTime;
}
