package com.yanmingchen.distributed.transaction.demo.entity.order;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 订单表
 * </p>
 *
 * @author YanmingChen
 * @since 2019-08-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TxOrder extends Model<TxOrder> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 订单号
     */
    private String code;

    /**
     * 购买人名称
     */
    private String buyerName;

    /**
     * 购买人电话
     */
    private String buyerPhone;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 购买数量
     */
    private Integer buyCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
