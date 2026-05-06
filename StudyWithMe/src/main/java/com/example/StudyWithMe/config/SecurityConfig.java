package com.example.StudyWithMe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/assignments/generate-ai").permitAll()
                        .anyRequest().authenticated()
                )

                // 3. 폼 로그인 설정 추가
                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login") // Postman에서 로그인할 때 쓸 주소
                        .usernameParameter("name")             // Member 엔티티의 필드명에 맞춤 (기본값 username)
                        .successHandler((request, response, authentication) -> {
                            response.setStatus(200);           // 로그인 성공 시 200 상태코드 반환
                        })
                        .failureHandler((request, response, exception) -> {
                            response.sendError(401, "Login Failed"); // 실패 시 401 반환
                        })
                )

                // 4. 세션 관리 설정 (기본값이지만 명시적 설정)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                );
        return http.build();
    }
}