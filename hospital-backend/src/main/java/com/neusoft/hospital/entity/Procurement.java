package com.neusoft.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("procurement")
public class Procurement {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private String drugName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String supplier;
    private Integer phase;
    private Integer urgent;
    private String logisticsNo;
    private String receiptNote;
    private Integer status;
    private LocalDateTime orderTime;
    private LocalDateTime createTime;

    @JsonProperty("orderDate")
    public String getOrderDate() {
        return orderTime != null ? orderTime.toLocalDate().toString() : null;
    }
}
