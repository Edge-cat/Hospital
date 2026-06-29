package com.neusoft.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("doctor")
public class Doctor {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String doctorNo;
    private String name;
    private Integer gender;
    private String department;
    private String title;
    private String specialty;
    private String phone;
    private Integer status;
    private LocalDateTime createTime;
}
