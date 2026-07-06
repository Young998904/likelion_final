package com.asmanage.repository;

import com.asmanage.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 고객 리포지토리.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // 접수 화면 자동완성: 고객명 또는 연락처 부분일치(최대 10건)
    List<Customer> findTop10ByNameContainingOrPhoneContainingOrderByIdDesc(String name, String phone);

    // 고객 포털 로그인: 전화번호로 조회(중복 대비 최신 1건)
    Optional<Customer> findFirstByPhoneOrderByIdDesc(String phone);

    // 포털 회원가입 중복 검사
    boolean existsByPhone(String phone);
}
