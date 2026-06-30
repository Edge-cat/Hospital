package com.neusoft.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("medical_record")
public class MedicalRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String department;
    private String diagnosis;
    private String treatment;
    /** 医嘱 JSON：检查/药品清单（医生开单无价格，护士确认后写入单价） */
    @TableField("order_items")
    private String orderItems;
    private LocalDateTime visitTime;
    private Integer status;
    private Integer revisionStatus;
    private String revisions;
    private LocalDateTime createTime;
}
