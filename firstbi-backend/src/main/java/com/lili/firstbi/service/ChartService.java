package com.lili.firstbi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lili.firstbi.model.dto.chart.ChartQueryRequest;
import com.lili.firstbi.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;


import javax.servlet.http.HttpServletRequest;

/**
* @author asus
* @description 针对表【chart(图表)】的数据库操作Service
* @createDate 2024-02-05 17:16:17
*/
public interface ChartService extends IService<Chart> {
    /**
     * 获取查询条件
     *
     * @param chartQueryRequest
     * @return
     */
    QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);

    void handleChartUpdateError(Long id,String message);

    void genChart(Long chartId,StringBuilder userInput);

    Boolean reloadChart(Long chartId, HttpServletRequest request);
}
