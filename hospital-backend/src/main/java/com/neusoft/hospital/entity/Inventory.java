package com.neusoft.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("inventory")
public class Inventory {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long drugId;
    private String drugCode;
    private String drugName;
    private Integer quantity;
    private Integer minStock;
    private String batchNo;
    private LocalDate expiryDate;
    private String warehouse;
    private Integer status;
    private LocalDateTime updateTime;
    private LocalDateTime createTime;

    @JsonProperty("stock")
    public Integer getStock() {
        return quantity;
    }
}
