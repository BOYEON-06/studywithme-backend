package com.example.StudyWithMe.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.session.web.http.SessionRepositoryFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final SessionRepositoryFilter<?> sessionRepositoryFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()) // 💡 핵심: 같은 도메인에서의 iframe 접근은 허용!
                )
                // 💡 [핵심] 주입받은 레디스 세션 필터를 시큐리티 최상단에 배치하여 톰캣의 세션 가로채기를 완벽히 막습니다.
                .addFilterBefore(sessionRepositoryFilter, org.springframework.security.web.context.SecurityContextHolderFilter.class)

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

                // 세션 보관소 명시 설정
                .securityContext(context -> context
                        .securityContextRepository(new HttpSessionSecurityContextRepository())
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/continue", "/api/auth/**").permitAll()

                        .requestMatchers("/api/studies/**").permitAll()
                        .requestMatchers("/api/assignments/**").permitAll()

                        .requestMatchers("/api/reservation-tasks/**").permitAll()

                        .requestMatchers("/api/chat/**").permitAll()

                        .requestMatchers("/ws-stomp/**").permitAll()
                        .requestMatchers("/report").permitAll()

                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
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