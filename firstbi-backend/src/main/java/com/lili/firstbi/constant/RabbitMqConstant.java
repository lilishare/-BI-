package com.lili.firstbi.constant;

public interface RabbitMqConstant {
     /**
     * ai genchart队列
     */
    public static final String BI_QUEUE_NAME = "chart_generate";
    public static final String BI_ROUTING_KEY = "bi_route";
    public static final String BI_EXCHANGE_NAME = "bi_exchange";
    public static final String BI_DIRECT_EXCHANGE = "direct";
    public static final String BI_HOST = "5672";
}
