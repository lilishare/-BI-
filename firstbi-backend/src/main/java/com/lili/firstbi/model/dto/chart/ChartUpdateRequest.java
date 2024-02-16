package com.lili.firstbi.model.dto.chart;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 更新请求
 *
 */
@Data
public class ChartUpdateRequest implements Serializable {
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
     * 生成图表数据
     */
    private String genChart;
    private static final long serialVersionUID = 1L;
}