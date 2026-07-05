package com.asmanage.repository;

import com.asmanage.domain.AsRepairItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * A/S 수리내역 라인 리포지토리.
 */
public interface AsRepairItemRepository extends JpaRepository<AsRepairItem, Long> {

    // 삭제 가드용: 해당 프리셋을 참조하는 수리내역 존재 여부
    boolean existsByPresetId(Long presetId);
}
