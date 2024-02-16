package com.lili.firstbi.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传请求
 */
@Data
public class GenChartRequest implements Serializable {

    /**
     * 图标名称
     */
    private String chartName;
    /**
     * 分析目标
     */
    private String goal;
    /**
     * 生成图表类型
     */
    private String chartType;
    private static final long serialVersionUID = 1L;
}