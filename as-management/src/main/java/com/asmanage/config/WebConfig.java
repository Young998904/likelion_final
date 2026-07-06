package com.asmanage.config;

import com.asmanage.security.CustomerAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 고객 포털 인증 인터셉터 등록.
 * 로그인/회원가입/정적 자원은 제외하고 나머지 /customer/** 경로를 보호한다.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final CustomerAuthInterceptor customerAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(customerAuthInterceptor)
                .addPathPatterns("/customer/**")
                .excludePathPatterns("/customer/login", "/customer/signup", "/css/**", "/js/**");
    }
}
