package com.asmanage.repository;

import com.asmanage.domain.AsRequest;
import com.asmanage.domain.AsStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * A/S 접수 리포지토리.
 */
public interface AsRequestRepository extends JpaRepository<AsRequest, Long> {

    // 상태별 접수 건수(대시보드 통계에 사용)
    long countByStatus(AsStatus status);

    // 미입금 접수 건수
    long countByPaidFalse();

    // 상태별 목록 조회
    List<AsRequest> findByStatus(AsStatus status);

    // 접수 목록(최근 접수순)
    List<AsRequest> findAllByOrderByCreatedAtDesc();

    // 삭제 가드용: 해당 고객/제품의 접수 이력 존재 여부
    boolean existsByCustomerId(Long customerId);
    boolean existsByProductId(Long productId);
}
