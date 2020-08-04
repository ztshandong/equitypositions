package com.zhangtao.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author :  张涛 zhangtao
 * @version :  1.0
 * @createDate :  2020/8/4 08:00
 * @description :
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host:127.0.0.1}")
    private String ip;

    @Value("${spring.redis.port:6379}")
    private int port;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(this.jedisPoolConfig());

        return jedisConnectionFactory;
    }

    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(500);
        jedisPoolConfig.setMaxIdle(5);
        jedisPoolConfig.setMaxWaitMillis(100);
        jedisPoolConfig.setTestOnBorrow(true);
        return jedisPoolConfig;
    }

    @Bean
    public JedisPool redisPoolFactory(JedisPoolConfig jedisPoolConfig) {
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, this.ip, this.port);

        return jedisPool;
    }

}
