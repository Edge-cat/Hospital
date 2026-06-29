package com.neusoft.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("medical_service")
public class MedicalService {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String serviceName;
    private String category;
    private String department;
    private BigDecimal price;
    private Integer duration;
    private Integer status;
    private String feeItem;
    private String description;
    private LocalDateTime createTime;
}
