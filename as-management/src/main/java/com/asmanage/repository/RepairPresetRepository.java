package com.asmanage.repository;

import com.asmanage.domain.RepairPreset;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 수리내역 프리셋 리포지토리.
 */
public interface RepairPresetRepository extends JpaRepository<RepairPreset, Long> {
}
