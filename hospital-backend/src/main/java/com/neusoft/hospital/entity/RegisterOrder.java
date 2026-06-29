package com.neusoft.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("register_order")
public class RegisterOrder {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String registerNo;
    private Long patientId;
    private String patientName;
    private String department;
    private Long doctorId;
    private String doctorName;
    private String registerType;
    private BigDecimal fee;
    private Integer status;
    private LocalDateTime registerTime;
    private LocalDateTime createTime;
}
