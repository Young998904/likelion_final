package com.asmanage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 세션 기반 인증/인가 설정.
 * 계정·마스터데이터 관리 화면(/admin/**)은 ADMIN 전용, 그 외는 로그인 필요.
 */
@Configuration
public class SecurityConfig {

    /**
     * 비밀번호 암호화기(BCrypt).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 보안 필터 체인 정의.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 로그인 화면, 정적 자원, H2 콘솔은 누구나 접근 가능
                        .requestMatchers("/login", "/css/**", "/js/**", "/h2-console/**").permitAll()
                        // 관리 화면(계정/고객/제품/프리셋 관리)은 ADMIN 전용
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // 나머지는 로그인한 직원만 접근
                        .anyRequest().authenticated()
                )
                // H2 콘솔은 CSRF 예외 + 프레임 허용 필요(개발 편의)
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .formLogin(form -> form
                        // 커스텀 로그인 화면 사용
                        .loginPage("/login")
                        // 로그인 성공 시 대시보드로 이동
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}
