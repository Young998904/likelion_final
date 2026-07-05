package com.asmanage.config;

import com.asmanage.domain.Employee;
import com.asmanage.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 모든 화면(@Controller)에 로그인한 직원의 표시 이름을 제공한다.
 * 상단 바에서 아이디 대신 직원 이름(예: 홍길동)을 노출하기 위함.
 */
@ControllerAdvice(annotations = Controller.class)
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final EmployeeRepository employeeRepository;

    @ModelAttribute("currentUserName")
    public String currentUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        // 아이디로 직원을 찾아 이름을 반환(없으면 아이디 그대로)
        return employeeRepository.findByUsername(auth.getName())
                .map(Employee::getName)
                .orElse(auth.getName());
    }
}
