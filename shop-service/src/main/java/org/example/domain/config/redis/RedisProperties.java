package org.example.domain.config.redis;

import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

// @Component
// @Getter
// @PropertySource("classpath:application.properties")
// public class RedisProperties {
//     @Value("${spring.data.redis.port}")
//     private int port;
//     @Value("${spring.data.redis.host}")
//     private String host;
// }

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisProperties {
    private String host;
    private int port;
}