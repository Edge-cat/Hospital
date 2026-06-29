package com.neusoft.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("dispensing")
public class Dispensing {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String dispensingNo;
    private String prescriptionNo;
    private String barcode;
    private String patientName;
    private String drugName;
    private Integer quantity;
    private String pharmacist;
    private String priority;
    private Integer status;
    private LocalDateTime dispensingTime;
    private LocalDateTime createTime;
}
