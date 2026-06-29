package com.neusoft.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("drug")
public class Drug {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String drugCode;
    private String drugName;
    private String specification;
    private String manufacturer;
    private String unit;
    private BigDecimal price;
    private String drugType;
    private String riskLevel;
    private Integer archived;
    private Integer status;
    private LocalDateTime createTime;
}
