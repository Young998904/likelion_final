package com.asmanage.repository;

import com.asmanage.domain.Employee;
import com.asmanage.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 직원 계정 리포지토리.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // 로그인 시 아이디로 계정 조회
    Optional<Employee> findByUsername(String username);

    // 아이디 중복 검사
    boolean existsByUsername(String username);

    // 담당자 지정 후보: 활성 직원 목록
    List<Employee> findByActiveTrueOrderByNameAsc();

    // 마지막 활성 최고관리자 보호용: 활성 상태의 특정 권한 계정 수
    long countByRoleAndActiveTrue(Role role);
}
