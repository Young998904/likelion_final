package com.asmanage.repository;

import com.asmanage.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 고객 리포지토리.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // 접수 화면 자동완성: 고객명 또는 연락처 부분일치(최대 10건)
    List<Customer> findTop10ByNameContainingOrPhoneContainingOrderByIdDesc(String name, String phone);
}
