package com.neusoft.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("bed")
public class Bed {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String bedNo;
    private String ward;
    private String bedType;
    private String department;
    private Integer status;
    private String patientName;
    private LocalDateTime createTime;
}
