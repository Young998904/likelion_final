package com.asmanage.repository;

import com.asmanage.domain.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 알림 로그 리포지토리.
 */
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    // 특정 접수 건의 알림 이력을 최신순으로 조회
    List<NotificationLog> findByRequestIdOrderByCreatedAtDesc(Long requestId);
}
