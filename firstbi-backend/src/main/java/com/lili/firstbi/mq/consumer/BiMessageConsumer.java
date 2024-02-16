//package com.lili.firstbi.mq.consumer;
//
//import com.lili.firstbi.constant.AiConstant;
//import com.lili.firstbi.manager.AiManagerByXh;
//import com.lili.firstbi.manager.RedisLimiterManager;
//import com.lili.firstbi.model.entity.Chart;
//import com.lili.firstbi.model.entity.User;
//import com.lili.firstbi.service.ChartService;
//import com.lili.firstbi.service.UserService;
//import com.rabbitmq.client.Channel;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.amqp.support.AmqpHeaders;
//import org.springframework.messaging.handler.annotation.Header;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.io.IOException;
//
//import static com.lili.firstbi.constant.RabbitMqConstant.BI_QUEUE_NAME;
//
///**
// * @projectName: firstbi-backend
// * @package: com.lili.firstbi.mq.product
// * @className: BiMessageConsumer
// * @author: lili
// * @description: TODO
// * @date: 2024/2/15 20:27
// * @version: 1.0
// */
//@Component
//@Slf4j
//public class BiMessageConsumer {
//    @Resource
//    private ChartService chartService;
//
//    @Resource
//    private RedisLimiterManager redisLimiterManager;
//
//    @Resource
//    private UserService userService;
//
//    // 指定程序监听的消息队列和确认机制
//    @SneakyThrows
//    @RabbitListener(queues = {BI_QUEUE_NAME}, ackMode = "MANUAL")
//    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
//        log.info("receiveMessage message = {}", message);
//        if (StringUtils.isBlank(message)) {
//            log.error("信息为空");
//            //空消息是没有价值的，直接确认
//            try {
//                channel.basicAck(deliveryTag, false);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        // 获取消息，使用ai处理数据
//        long chartID = Long.parseLong(message);
//        Chart chart = chartService.getById(chartID);
//        String chartType = chart.getChartType();
//        String goal = chart.getGoal();
//        String chartData = chart.getChartData();
//        Long userId = chart.getUserId();
//        User loginUser = userService.getById(userId);
//
//        // 构造用户输入
//        StringBuilder userInput = new StringBuilder();
//        // 拼接分析目标
//        String userGoal = goal;
//        if (StringUtils.isNotBlank(chartType)) {
//            userGoal += "，设置type的类型为" + chartType;
//        }
//        userInput.append(userGoal).append("\n");
//        userInput.append(AiConstant.REQUEST_SPLIT);
//        userInput.append(chartData).append("\n");
//        // 限流
//        redisLimiterManager.doRateLimit("getChartByThread" + loginUser.getId());
//        chartService.genChart(chartID, userInput);
//        // 消息确认
//        channel.basicAck(deliveryTag, false);
//
//    }
//}
