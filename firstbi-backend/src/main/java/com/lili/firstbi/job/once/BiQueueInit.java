package com.lili.firstbi.job.once;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import static com.lili.firstbi.constant.RabbitMqConstant.*;

/**
 * @projectName: firstbi-backend
 * @package: com.lili.firstbi.job.once
 * @className: BiQueueInit
 * @author: lili
 * @description: 初始化bi消息队列
 * @date: 2024/2/15 18:18
 * @version: 1.0
 */
//@Component
@Slf4j

public class BiQueueInit implements CommandLineRunner {


    @Override
    public void run(String... args) throws Exception {

        try  {
            ConnectionFactory factory = new ConnectionFactory();
            Connection connection = factory.newConnection();
            factory.setHost(BI_HOST);
            Channel channel;
            channel = connection.createChannel();
            channel.exchangeDeclare(BI_EXCHANGE_NAME,BI_DIRECT_EXCHANGE);

            // 创建队列，随机分配一个队列名称
            /**
             * durable：指示队列是否持久化。如果设置为true，则RabbitMQ会将队列保存到磁盘上，以便在服务器重启后恢复。如果设置为false，则消息仅存在于内存中，服务器重启时会丢失。默认为false。
             * exclusive：指示队列是否为专有队列。如果设置为true，则只有声明队列的连接可以使用该队列。一旦连接关闭，队列就会自动删除。默认为false。
             * autoDelete：指示队列是否为自动删除队列。如果设置为true，则队列在消费者断开连接时会自动删除。默认为false。
             * arguments：用于设置一些额外的参数。它是一个Map类型的参数，可以根据需要传递一些额外的参数。例如，可以设置队列的最大长度、超时时间等。
             */
            channel.queueDeclare(BI_QUEUE_NAME, true, false, false, null);
            channel.queueBind(BI_QUEUE_NAME, BI_DIRECT_EXCHANGE, BI_ROUTING_KEY);
        }catch (Exception e){
            log.error(String.valueOf(e));
        }



    }
}
