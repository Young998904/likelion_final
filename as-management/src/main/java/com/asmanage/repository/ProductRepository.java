package com.asmanage.repository;

import com.asmanage.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 제품 리포지토리.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 접수 화면 자동완성: 제품명 또는 모델코드 부분일치(최대 10건)
    List<Product> findTop10ByNameContainingOrModelCodeContainingOrderByIdDesc(String name, String modelCode);
}
