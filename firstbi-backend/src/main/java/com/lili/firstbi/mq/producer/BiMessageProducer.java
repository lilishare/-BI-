package com.lili.firstbi.mq.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.lili.firstbi.constant.RabbitMqConstant.BI_EXCHANGE_NAME;
import static com.lili.firstbi.constant.RabbitMqConstant.BI_ROUTING_KEY;

/**
 * @projectName: firstbi-backend
 * @package: com.lili.firstbi.mq.consumer
 * @className: BiMessageConsumer
 * @author: lili
 * @description: TODO
 * @date: 2024/2/15 20:25
 * @version: 1.0
 */
@Component
@Slf4j
public class BiMessageProducer {
        @Resource
        private RabbitTemplate rabbitTemplate;

        /**
         * 发送消息
         * @param message
         */
        public void sendMessage(String message) {
            rabbitTemplate.convertAndSend(BI_EXCHANGE_NAME, BI_ROUTING_KEY, message);
        }

    }


