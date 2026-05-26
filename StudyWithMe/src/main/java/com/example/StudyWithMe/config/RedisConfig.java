package com.example.StudyWithMe.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800) // 세션 만료 시간 30분
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    /*
     * 🕵️‍♂️ [핵심 포인트]
     * 원래 있던 springSessionDefaultRedisSerializer() 빈은 아예 삭제했습니다!
     * 이렇게 지워버리면, 스프링 시큐리티는 알아서 가장 안전한 '기본 직렬화기(JDK)'를 써서
     * 복잡한 로그인 정보를 서버 1, 2번 사이에서 완벽하게 공유합니다. (401 에러 해결!)
     */

    /**
     * 🚀 개발자가 소스코드에서 캐시 등 다용도로 직접 사용할 RedisTemplate
     * (이건 로그인 세션과는 완전히 별개로 동작하므로 안심하고 써도 됩니다!)
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        redisTemplate.setValueSerializer(new org.springframework.data.redis.serializer.JdkSerializationRedisSerializer());
        redisTemplate.setHashValueSerializer(new org.springframework.data.redis.serializer.JdkSerializationRedisSerializer());

        return redisTemplate;
    }
}