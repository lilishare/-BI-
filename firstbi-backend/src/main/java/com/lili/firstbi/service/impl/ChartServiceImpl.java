package com.lili.firstbi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lili.firstbi.common.ErrorCode;
import com.lili.firstbi.constant.CommonConstant;
import com.lili.firstbi.exception.BusinessException;
import com.lili.firstbi.exception.ThrowUtils;
import com.lili.firstbi.manager.AiManagerByXh;
import com.lili.firstbi.mapper.ChartMapper;
import com.lili.firstbi.model.dto.chart.ChartQueryRequest;
import com.lili.firstbi.model.entity.Chart;
import com.lili.firstbi.model.entity.User;
import com.lili.firstbi.model.enums.ExecStatusEnum;
import com.lili.firstbi.mq.producer.BiMessageProducer;
import com.lili.firstbi.service.ChartService;
import com.lili.firstbi.service.UserService;
import com.lili.firstbi.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author asus
 * @description 针对表【chart(图表)】的数据库操作Service实现
 * @createDate 2024-02-05 17:16:17
 */
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
        implements ChartService {
    @Resource
    private AiManagerByXh aiManagerByXh;
    @Resource
    private UserService userService;
    @Resource
    private BiMessageProducer biMessageProducer;

    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        String chartType = chartQueryRequest.getChartType();
        String goal = chartQueryRequest.getGoal();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();
        Long id = chartQueryRequest.getId();
        Long userId = chartQueryRequest.getUserId();
        String chartName = chartQueryRequest.getChartName();

        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(chartName), "chartName", chartName);
        queryWrapper.like(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(StringUtils.isNotEmpty(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public void handleChartUpdateError(Long id, String message) {
        Chart chart = new Chart();
        chart.setId(id);
        chart.setExecStatus(ExecStatusEnum.FAILED.getValue());
        chart.setExecMessage(message);
        boolean b = this.updateById(chart);
        if (!b) {
            log.error("无法将更改图表生成失败");
        }
    }

    @Override
    public void genChart(Long chartId, StringBuilder userInput) {
        String result = aiManagerByXh.doChart(userInput.toString());
        result = result.replaceAll("```json", "");
        result = result.replaceAll("```", "");
        // 压缩json
        result = result.replaceAll("\t+", "");
        result = result.replaceAll(" +", "");
        result = result.replaceAll("\n+", "");
        // 提取图表数据
        int chartLen = "【图表】".length();
        int conclusionLen = "【结论】".length();
        int chartStartIndex = result.indexOf("【图表】");
        int chartEndIndex = result.indexOf("【结论】");
        if (chartStartIndex == -1 || chartEndIndex == -1) {
            this.handleChartUpdateError(chartId, "图表状态更新失败");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Ai生成错误");
        }

        String genChart = result.substring(chartStartIndex + chartLen, chartEndIndex);
        // 提取结论
        String genResult = result.substring(chartEndIndex + conclusionLen);

        // 插入到数据库
        Chart chart = new Chart();
        chart.setId(chartId);
        chart.setExecStatus(ExecStatusEnum.SUCCEED.getValue());
        chart.setExecMessage("图表生成成功");
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        boolean saveResult = this.updateById(chart);
        if (!saveResult) {
            this.handleChartUpdateError(chartId, "图表状态更新失败");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图表状态更新失败");
        }
    }

    @Override
    public Boolean reloadChart(Long chartId, HttpServletRequest request) {
        ThrowUtils.throwIf(chartId < 0, ErrorCode.PARAMS_ERROR, "参数错误");
        //发送消息
        biMessageProducer.sendMessage(String.valueOf(chartId));
        return true;


    }
}




