package com.lili.firstbi.config;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @projectName: firstbi-backend
 * @package: com.lili.firstbi.config
 * @className: ThreadPoolExecutorConfig
 * @author: lili
 * @description: TODO
 * @date: 2024/2/12 22:55
 * @version: 1.0
 */
@Configuration
@Data
public class ThreadPoolExecutorConfig {
    final int corePoolSize = 1;
    final int maximumPoolSize = 2;
    final long keepAliveTime = 1800;
    final TimeUnit unit = TimeUnit.SECONDS;
    @Bean
    ThreadPoolExecutor threadPoolExecutor() {
        ThreadFactory threadFactory = new ThreadFactory() {
            private int count = 1;


            @Override
            public Thread newThread(@NotNull Runnable r) {
                return new Thread(r, "thread-" + count++);
            }
        };
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit,
                new ArrayBlockingQueue<>(4), threadFactory);
    }
}
