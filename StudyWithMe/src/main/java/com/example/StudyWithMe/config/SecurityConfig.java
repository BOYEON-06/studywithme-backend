package com.example.StudyWithMe.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();

        // 1. 프론트엔드가 익숙한 JSESSIONID로 이름 맞추기 (선택사항)
        serializer.setCookieName("JSESSIONID");

        // 2. HTTP 환경(IP 주소)에서 포스트맨/브라우저가 튕겨내지 않도록 허용! (핵심)
        serializer.setUseSecureCookie(false);

        // 3. 타 도메인(IP) 접근 허용
        serializer.setSameSite("Lax");

        // 4. 모든 API 경로(/)에서 쿠키 들고 다니게 허용
        serializer.setCookiePath("/");

        return serializer;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CORS 설정을 Security 필터 체인에 추가
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 인증 실패 시 401 에러 반환
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                )

                .authorizeHttpRequests(auth -> auth
                        // OPTIONS 메서드는 CORS를 위해 모두 허용
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/api/auth/continue").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/assignments/**").permitAll()
                        .anyRequest().authenticated()
                )

//                .formLogin(form -> form
//                        .loginProcessingUrl("/api/auth/login")
//                        .usernameParameter("name")
//                        .successHandler((request, response, authentication) -> {
//                            response.setStatus(200);
//                        })
//                        .failureHandler((request, response, exception) -> {
//                            response.sendError(401, "Login Failed");
//                        })
//                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 프론트엔드 실제 주소를 명시
        configuration.addAllowedOrigin("http://localhost:5173");
        configuration.addAllowedOriginPattern("http://localhost:5173");
        configuration.addAllowedOrigin("http://34.22.71.200");
        configuration.addAllowedOrigin("http://34.22.71.200:9090");
        configuration.addAllowedOriginPattern("http://localhost:9090");

        // 2. 모든 메서드와 헤더 허용
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");

        // 3. 자격 증명(쿠키, 세션 등) 허용
        configuration.setAllowCredentials(true);

        // 4. 브라우저가 응답 헤더에 접근할 수 있도록 노출 (필요 시)
        configuration.addExposedHeader("Set-Cookie");
        configuration.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}