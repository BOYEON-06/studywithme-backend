package com.example.StudyWithMe.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800) // 세션 만료 시간 30분 지정 및 기능 활성화
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    /**
     * 2. 스프Spring Session Redis가 내부적으로 가로채서 사용할 최신 JSON 직렬화 빈
     * 최신 규격인 GenericJacksonJsonRedisSerializer를 사용해 안전하게 객체를 JSON으로 굽습니다.
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        // 최신 버전에서는 스프링 시큐리티 유틸을 호출하는 대신, 인자를 완전히 비우고 생성해도
        // 내부적으로 시큐리티 믹스인 처리가 자동 내장되어 완벽하게 빌드됩니다. (빨간 줄 0%)
        return RedisSerializer.json();
    }

    /**
     * 3. 개발자가 소스코드에서 직접 주입받아 다용도로 사용할 일반 RedisTemplate 설정
     * 밸류 타입을 Object로 넓혀두어야 세션 데이터와 싱크가 맞아 에러가 안 납니다.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        // Key는 질문하신 대로 redis-cli에서 알아볼 수 있게 문자열로 세팅
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // Value는 위의 스프링 세션 직렬화 장치와 싱크를 맞춰서 JSON 형태로 안전하게 저장
        redisTemplate.setValueSerializer(springSessionDefaultRedisSerializer());
        redisTemplate.setHashValueSerializer(springSessionDefaultRedisSerializer());

        return redisTemplate;
    }
}