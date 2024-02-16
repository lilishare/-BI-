package com.lili.firstbi.manager;

import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.lili.firstbi.common.ErrorCode;
import com.lili.firstbi.config.XfXhConfig;
import com.lili.firstbi.constant.AiConstant;
import com.lili.firstbi.exception.BusinessException;
import com.lili.firstbi.listener.XfXhWebSocketListener;
import com.lili.firstbi.model.dto.AI.MsgDTO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.UUID;

/**
 * @projectName: firstbi-backend
 * @package: com.lili.firstbi.manager
 * @className: AiManagerByXh
 * @author: lili
 * @description: 调用星火大模型
 * @date: 2024/2/10 15:02
 * @version: 1.0
 */
@Service
@Slf4j
public class AiManagerByXh {
    @Resource
    private XfXhStreamClient xfXhStreamClient;

    @Resource
    private XfXhConfig xfXhConfig;

    public String doChart(String message) {
        // 根据message获取分析需求与数据
        String[] splits = message.split(AiConstant.REQUEST_SPLIT);
        String goal = splits[0];
        String data = splits[1];
        String question = String.format("你是一个数据分析师和前端开发专家" +
                "接下来我会按照以下固定格式给你提供内容：分析需求：{%s}" +
                "原始数据：{%s}" +
                "请根据这两部分内容，按照以下指定格式生成内容,只要代码与结论，不要分析数据（此外不要输出任何多余内容）" +
                "【图表】-{前端 Echarts V5 的 option 配置对象，使用json格式，合理地将数据进行可视化，data项中不能出现表达式，不要生成任何多余的内容，比如注释}【图表】" +
                "【结论】-{明确的数据分析结论、越详细越好，不要生成多余的注释}【结论】", goal, data);

        if (StrUtil.isBlank(question)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效问题，请重新输入");
        }
        // 获取连接令牌
        if (!xfXhStreamClient.operateToken(XfXhStreamClient.GET_TOKEN_STATUS)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "当前大模型连接数过多，请稍后再试");
        }

        // 创建消息对象
        MsgDTO msgDTO = MsgDTO.createUserMsg(question);
        // 创建监听器
        XfXhWebSocketListener listener = new XfXhWebSocketListener();
        // 发送问题给大模型，生成 websocket 连接
        WebSocket webSocket = xfXhStreamClient.sendMsg(UUID.randomUUID().toString().substring(0, 10), Collections.singletonList(msgDTO), listener);
        if (webSocket == null) {
            // 归还令牌
            xfXhStreamClient.operateToken(XfXhStreamClient.BACK_TOKEN_STATUS);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统内部错误，请联系管理员");
        }
        try {
            int count = 0;
            // 为了避免死循环，设置循环次数来定义超时时长
            int cnt = question.length() / 20;
            int maxCount = xfXhConfig.getMaxResponseTime() * cnt;
            while (count <= maxCount) {
                Thread.sleep(200);
                if (listener.isWsCloseFlag()) {
                    break;
                }
                count++;
            }
            if (count > maxCount) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "大模型响应超时，请联系管理员");
            }

        } catch (InterruptedException e) {
            log.error("错误：" + e.getMessage());
//            return "系统内部错误，请联系管理员";
        } finally {
            // 关闭 websocket 连接
            webSocket.close(1000, "");
            // 归还令牌
            xfXhStreamClient.operateToken(XfXhStreamClient.BACK_TOKEN_STATUS);
        }
        // 响应大模型的答案
        System.out.println(listener.getAnswer().toString());
        return listener.getAnswer().toString();
    }


}
