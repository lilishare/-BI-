package com.lili.firstbi.mq.consumer;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import static com.lili.firstbi.constant.RabbitMqConstant.BI_QUEUE_NAME;

/**
 * @projectName: firstbi-backend
 * @package: com.lili.firstbi.mq.consumer
 * @className: MyConsumer
 * @author: lili
 * @description: TODO
 * @date: 2024/2/15 21:21
 * @version: 1.0
 */
@Component
@Slf4j
public class MyConsumer {
        // 指定程序监听的消息队列和确认机制
        @SneakyThrows
        @RabbitListener(queues = {BI_QUEUE_NAME}, ackMode = "MANUAL")
        public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
            log.info("receiveMessage message = {}", message);
            channel.basicAck(deliveryTag, false);
        }

    }


