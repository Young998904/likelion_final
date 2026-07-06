package com.asmanage.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 고객 포털 접근 제어. 세션에 고객 id가 없으면 로그인 화면으로 보낸다.
 * (직원용 Spring Security와 분리된 경량 인증)
 */
@Component
public class CustomerAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (CustomerSession.getId(request.getSession()) != null) {
            return true;
        }
        response.sendRedirect(request.getContextPath() + "/customer/login");
        return false;
    }
}
