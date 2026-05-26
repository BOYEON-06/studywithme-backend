package com.example.StudyWithMe.config;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.web.filter.DelegatingFilterProxy;
import tools.jackson.databind.DatabindContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800)
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("JSESSIONID");
        serializer.setCookiePath("/");
        serializer.setUseBase64Encoding(false); // 순수 UUID 텍스트 오가도록 설정
        return serializer;
    }

    @Bean
    public FilterRegistrationBean<DelegatingFilterProxy> springSessionRepositoryFilterRegistration() {
        FilterRegistrationBean<DelegatingFilterProxy> registration = new FilterRegistrationBean<>();
        registration.setFilter(new DelegatingFilterProxy("springSessionRepositoryFilter"));
        registration.addUrlPatterns("/*");
        registration.setOrder(Integer.MIN_VALUE); // 최우선 순위로 필터 배치
        registration.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC);
        return registration;
    }

    /**
     * 1. Jackson 3.x 규격의 ObjectMapper 빈 등록
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ClassLoader loader = getClass().getClassLoader();

        PolymorphicTypeValidator ptv = new PolymorphicTypeValidator() {
            @Override
            public Validity validateBaseType(DatabindContext ctxt, JavaType baseType) { return Validity.ALLOWED; }
            @Override
            public Validity validateSubClassName(DatabindContext ctxt, JavaType baseType, String subClassName) { return Validity.ALLOWED; }
            @Override
            public Validity validateSubType(DatabindContext ctxt, JavaType baseType, JavaType subType) { return Validity.ALLOWED; }
        };

        return JsonMapper.builder()
                // [공식 문서 마이그레이션 가이드 적용]
                // 1. 3.x 규칙에 따라 tools.jackson.databind.DefaultTyping을 직접 경유합니다.
                // 2. 스프링 시큐리티 컨텍스트 복원을 위해 NON_CONCRETE_AND_ARRAYS 스코프와 프로퍼티 속성명("@class")을 명시합니다.
                .activateDefaultTypingAsProperty(
                        ptv,
                        tools.jackson.databind.DefaultTyping.NON_CONCRETE_AND_ARRAYS,
                        "@class"
                )
                .addModules(SecurityJacksonModules.getModules(loader))
                .build();
    }

    /**
     * 2. 💡 스프링 세션 레디스 전용 직렬화기 최종 바인딩
     * (메서드 중복 제거 및 Jackson 3.x에 대응하는 GenericJacksonJsonRedisSerializer 채택)
     */
    @Bean(name = "springSessionDefaultRedisSerializer")
    public RedisSerializer<Object> springSessionDefaultRedisSerializer(ObjectMapper redisObjectMapper) {
        // 💡 우리가 커스텀한 redisObjectMapper를 내장시켜 Map 뭉개짐을 완벽 방어합니다.
        return new GenericJacksonJsonRedisSerializer(redisObjectMapper);
    }

    /**
     * 3. 일반 개발용 RedisTemplate 설정
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            RedisSerializer<Object> springSessionDefaultRedisSerializer
    ) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // 세션 직렬화 장치와 싱크를 완벽히 일치시킴
        redisTemplate.setValueSerializer(springSessionDefaultRedisSerializer);
        redisTemplate.setHashValueSerializer(springSessionDefaultRedisSerializer);

        return redisTemplate;
    }
}