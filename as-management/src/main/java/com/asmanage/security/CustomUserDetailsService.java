package com.asmanage.security;

import com.asmanage.domain.Employee;
import com.asmanage.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 로그인 시 직원 계정을 조회하여 Spring Security 인증 정보로 변환한다.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 아이디로 계정 조회(없으면 로그인 거부)
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 계정입니다: " + username));

        // 권한은 "ROLE_" 접두사를 붙여 부여(예: ROLE_ADMIN)
        return User.builder()
                .username(employee.getUsername())
                .password(employee.getPassword())
                .roles(employee.getRole().name())
                // 비활성 계정은 로그인 거부(disabled 처리)
                .disabled(!employee.isActive())
                .build();
    }
}
