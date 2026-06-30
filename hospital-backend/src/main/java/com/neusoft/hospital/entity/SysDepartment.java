package com.neusoft.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_department")
public class SysDepartment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    /** 科室编码（如 NK=内科） */
    private String code;
    private Long parentId;
    private Integer sort;
    private Integer status;
    private String description;
    private LocalDateTime createTime;
}
