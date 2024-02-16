package com.lili.firstbi.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.lili.firstbi.config.XfXhConfig;
import com.lili.firstbi.listener.XfXhWebSocketListener;
import com.lili.firstbi.manager.BigModelNew;
import com.lili.firstbi.manager.XfXhStreamClient;
import com.lili.firstbi.model.dto.AI.MsgDTO;
import com.lili.firstbi.mq.producer.BiMessageProducer;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * EasyExcel 测试
 */
@SpringBootTest
@Slf4j
public class EasyExcelTest {
    @Resource
    private BiMessageProducer biMessageProducer;
    @Test
    public void t() {
        biMessageProducer.sendMessage("hello");
    }


    @Resource
    private XfXhStreamClient xfXhStreamClient;

    @Resource
    private XfXhConfig xfXhConfig;

    @Test
    public void testyufa() {
      String text = "```json\n" +
              "{\n" +
              "  \"title\": {\n" +
              "    \"text\": \"商品利润分析-面积图\"\n" +
              "  },";
        String s = text.replaceAll("```json","");
        System.out.println(s);
    }



    @Test
    public void testxh() {
        String question = "python生成1";
        if (StrUtil.isBlank(question)) {
            System.out.println("无效问题，请重新输入");
            return;
        }
        // 获取连接令牌
        if (!xfXhStreamClient.operateToken(XfXhStreamClient.GET_TOKEN_STATUS)) {
//            return "当前大模型连接数过多，请稍后再试";
            System.out.println("当前大模型连接数过多，请稍后再试");
            return;
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
            System.out.println("系统内部错误，请联系管理员");
            return;
        }
        try {
            int count = 0;
            // 为了避免死循环，设置循环次数来定义超时时长
            int maxCount = xfXhConfig.getMaxResponseTime() * 5;
            while (count <= maxCount) {
                Thread.sleep(200);
                if (listener.isWsCloseFlag()) {
                    break;
                }
                count++;
            }
            if (count > maxCount) {
                System.out.println("大模型响应超时，请联系管理员");
                return;
            }
            // 响应大模型的答案
            System.out.println(listener.getAnswer().toString());
//            return listener.getAnswer().toString();
        } catch (InterruptedException e) {
            log.error("错误：" + e.getMessage());
//            return "系统内部错误，请联系管理员";
        } finally {
            // 关闭 websocket 连接
            webSocket.close(1000, "");
            // 归还令牌
            xfXhStreamClient.operateToken(XfXhStreamClient.BACK_TOKEN_STATUS);
        }
    }

    @Test
    public void doImport() throws FileNotFoundException {
        File file = ResourceUtils.getFile("classpath:test_excel.xlsx");
        StringBuffer result = new StringBuffer();
        List<Map<Integer, String>> list = EasyExcel.read(file)
                .excelType(ExcelTypeEnum.XLSX)
                .sheet()
                .headRowNumber(0)
                .doReadSync();

        LinkedHashMap<Integer, String> headMap = (LinkedHashMap<Integer, String>) list.get(0);
        List<String> headList = headMap.values().stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        result.append(StringUtils.join(headList, ",")).append("\n");
        for (int i = 1; i < list.size(); i++) {
            LinkedHashMap<Integer, String> dataMap = (LinkedHashMap<Integer, String>) list.get(i);
            List<String> dataList = dataMap.values().stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
            result.append(StringUtils.join(dataList, ","));
        }
        System.out.println(result);


    }


}