package com.lili.firstbi.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 图表
 * @TableName chart
 */
@TableName(value ="chart")
@Data
public class Chart implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 图标名称
     */
    private String chartName;
    /**
     * 分析目标
     */
    private String goal;

    /**
     * 提供的图表数据
     */
    private String chartData;

    /**
     * 生成图表类型
     */
    private String chartType;

    /**
     * 生成的图表数据
     */
    private String genChart;

    /**
     * 生成的图表分析
     */
    private String genResult;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 执行状态
     */
    private int execStatus;
    /**
     * 执行信息
     */
    private String  execMessage;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}