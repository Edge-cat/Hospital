package com.neusoft.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("schedule")
public class Schedule {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long doctorId;
    private String doctorName;
    private String department;
    private LocalDate shiftDate;
    private String shiftType;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
}
