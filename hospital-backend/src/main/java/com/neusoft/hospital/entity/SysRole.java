package com.neusoft.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_role")
public class SysRole {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;

    @JsonProperty("roleName")
    public String getRoleName() {
        return name;
    }

    @JsonProperty("roleCode")
    public String getRoleCode() {
        return code;
    }
    private String description;
    private Integer status;
    private LocalDateTime createTime;
}
