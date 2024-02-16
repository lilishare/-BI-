package com.lili.firstbi.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @projectName: firstbi-backend
 * @package: com.lili.firstbi.config
 * @className: RedissonConfig
 * @author: lili
 * @description: TODO
 * @date: 2024/2/12 9:29
 * @version: 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "spring.datasource.redis")
@Data
public class RedissonConfig {

    private Integer database;

    private String host;

    private Integer port;

    private String password;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setDatabase(database)
                .setAddress("redis://" + host + ":" + port);
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
