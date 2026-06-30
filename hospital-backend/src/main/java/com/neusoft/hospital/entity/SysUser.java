package com.neusoft.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    @JsonIgnore
    private String password;
    private String name;
    private String role;
    private String roleLabel;
    private String department;
    private String phone;
    private Integer status;
    private LocalDateTime createTime;

    /** 管理端表单角色键（admin/doctor/nurse/finance/pharmacy），不入库 */
    @TableField(exist = false)
    private String roleKey;
}
