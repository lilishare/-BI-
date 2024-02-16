package com.lili.firstbi.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lili.firstbi.annotation.AuthCheck;
import com.lili.firstbi.common.BaseResponse;
import com.lili.firstbi.common.DeleteRequest;
import com.lili.firstbi.common.ErrorCode;
import com.lili.firstbi.common.ResultUtils;
import com.lili.firstbi.constant.AiConstant;
import com.lili.firstbi.constant.UserConstant;
import com.lili.firstbi.exception.BusinessException;
import com.lili.firstbi.exception.ThrowUtils;
import com.lili.firstbi.manager.AiManagerByXh;
import com.lili.firstbi.manager.RedisLimiterManager;
import com.lili.firstbi.model.dto.chart.*;
import com.lili.firstbi.model.entity.Chart;
import com.lili.firstbi.model.entity.User;
import com.lili.firstbi.model.enums.ExecStatusEnum;
import com.lili.firstbi.model.vo.BiResponse;
import com.lili.firstbi.mq.producer.BiMessageProducer;
import com.lili.firstbi.service.ChartService;
import com.lili.firstbi.service.UserService;
import com.lili.firstbi.utils.ExcelUtils;
import com.lili.firstbi.utils.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * 帖子接口
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;
    @Resource
    private AiManagerByXh aiManagerByXh;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private BiMessageProducer biMessageProducer;

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
//        if(loginUser==null)
//            throw  new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * @param multipartFile
     * @param genChartRequest
     * @param request
     * @return
     */
    @PostMapping("/getChart")
    public BaseResponse<BiResponse> getChart(GenChartRequest genChartRequest, @RequestPart("file") MultipartFile multipartFile, HttpServletRequest request) {
        // 检验文件安全
        FileUtil.checkExcelFile(multipartFile, 1);

        // 预处理输入数据
        String chartName = genChartRequest.getChartName();
        chartName = chartName == null ? "分析结果" : chartName;
        String chartType = genChartRequest.getChartType();
        String goal = genChartRequest.getGoal();
        // 名称50字以内
        ThrowUtils.throwIf(chartName.length() > 50, ErrorCode.PARAMS_ERROR, "图表名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标为空");
        ThrowUtils.throwIf(goal.length() > 200, ErrorCode.PARAMS_ERROR, "分析目标过长");
//        long biModelId = 1755551486563467266L;
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，设置type的类型为" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append(AiConstant.REQUEST_SPLIT);
        String csvData = ExcelUtils.toCSV(multipartFile);
        userInput.append(csvData).append("\n");
        // 使用星火大模型
        // 限流
        redisLimiterManager.doRateLimit("getChart" + loginUser.getId());
        // 调用AI
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
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Ai生成错误");
        }
        // 提取图表
        String genChart = result.substring(chartStartIndex + chartLen, chartEndIndex);
        // 提取结论
        String genResult = result.substring(chartEndIndex + conclusionLen);
        // 插入到数据库
        Chart chart = new Chart();
        chart.setChartName(chartName);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }

    /**
     * @param multipartFile
     * @param genChartRequest
     * @param request
     * @return
     * @TODO 使用线程池方法异步生成图表
     */
    @PostMapping("/getChart/async")
    public BaseResponse<BiResponse> getChartByThread(GenChartRequest genChartRequest, @RequestPart("file") MultipartFile multipartFile, HttpServletRequest request) {
        // 检验文件安全
        FileUtil.checkExcelFile(multipartFile, 1);

        // 预处理输入数据
        String chartName = genChartRequest.getChartName();
        chartName = chartName == null ? "分析结果" : chartName;
        String chartType = genChartRequest.getChartType();
        String goal = genChartRequest.getGoal();
        // 名称50字以内
        ThrowUtils.throwIf(chartName.length() > 50, ErrorCode.PARAMS_ERROR, "图表名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标为空");
        ThrowUtils.throwIf(goal.length() > 200, ErrorCode.PARAMS_ERROR, "分析目标过长");
//        long biModelId = 1755551486563467266L;
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，设置type的类型为" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append(AiConstant.REQUEST_SPLIT);
        String csvData = ExcelUtils.toCSV(multipartFile);
        userInput.append(csvData).append("\n");
        // 插入到数据库
        Chart chart = new Chart();
        chart.setChartName(chartName);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setExecStatus(ExecStatusEnum.RUNNING.getValue());
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);

        // 获取生成的图表id
        Long chartId = chart.getId();
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "保存图表是失败");
        // 使用星火大模型
        try {
            CompletableFuture.runAsync(() -> {
                Chart updateChart = new Chart();
                updateChart.setId(chart.getId());
                updateChart.setExecStatus(ExecStatusEnum.RUNNING.getValue());
                boolean updateRes = chartService.updateById(updateChart);
                if (!updateRes) {
                    chartService.handleChartUpdateError(chartId,"图表状态更新失败");
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图表状态更新失败");
                }
                // 限流
                redisLimiterManager.doRateLimit("getChartByThread" + loginUser.getId());
                chartService.genChart(chartId, userInput);
            },threadPoolExecutor);

        } catch (Exception e) {
            System.out.println(e);
        }
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chartId);
        return ResultUtils.success(biResponse);
    }
    /**
     * @param multipartFile
     * @param genChartRequest
     * @param request
     * @return
     * @TODO 使用消息队列方法异步生成图表
     */
    @PostMapping("/getChart/mq")
    public BaseResponse<BiResponse> getChartByMQ(GenChartRequest genChartRequest, @RequestPart("file") MultipartFile multipartFile, HttpServletRequest request) {
        // 检验文件安全
        FileUtil.checkExcelFile(multipartFile, 1);

        // 预处理输入数据
        String chartName = genChartRequest.getChartName();
        chartName = chartName == null ? "分析结果" : chartName;
        String chartType = genChartRequest.getChartType();
        String goal = genChartRequest.getGoal();
        // 名称50字以内
        ThrowUtils.throwIf(chartName.length() > 50, ErrorCode.PARAMS_ERROR, "图表名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标为空");
        ThrowUtils.throwIf(goal.length() > 200, ErrorCode.PARAMS_ERROR, "分析目标过长");
//        long biModelId = 1755551486563467266L;
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        String csvData = ExcelUtils.toCSV(multipartFile);
        // 插入到数据库
        Chart chart = new Chart();
        chart.setChartName(chartName);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setExecStatus(ExecStatusEnum.RUNNING.getValue());
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);

        // 获取生成的图表id
        Long chartId = chart.getId();
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "保存图表是失败");
        // 发送消息给队列
        try {
            biMessageProducer.sendMessage(String.valueOf(chartId));

        } catch (Exception e) {
            chartService.handleChartUpdateError(chart.getId(), "Ai生成图表失败" + e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Ai生成图表失败");
        }
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chartId);
        return ResultUtils.success(biResponse);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }
    @GetMapping("/reload")
    public BaseResponse<Boolean> reloadChart(Long chartId,HttpServletRequest request){
        return ResultUtils.success(chartService.reloadChart(chartId,request));

    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        chart.setExecStatus(ExecStatusEnum.SUCCEED.getValue());
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

//    /**
//     * 分页获取列表（仅管理员）
//     *
//     * @param chartQueryRequest
//     * @return
//     */
//    @PostMapping("/list/page")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
//    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest) {
//        long current = chartQueryRequest.getCurrent();
//        long size = chartQueryRequest.getPageSize();
//        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
//                chartService.getQueryWrapper(chartQueryRequest));
//        return ResultUtils.success(chartPage);
//    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                     HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartVOByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                         HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    // endregion

    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);
        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

}
