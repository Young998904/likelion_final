package com.asmanage.service;

import com.asmanage.domain.RepairPreset;
import com.asmanage.dto.RepairPresetForm;
import com.asmanage.repository.AsRepairItemRepository;
import com.asmanage.repository.RepairPresetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 수리내역 프리셋 관리 비즈니스 로직.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepairPresetService {

    private final RepairPresetRepository repairPresetRepository;
    private final AsRepairItemRepository asRepairItemRepository;

    /** 전체 프리셋을 최근 등록순으로 조회. */
    public List<RepairPreset> findAll() {
        return repairPresetRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    /** 수정 화면용 폼 조회. */
    public RepairPresetForm getForm(Long id) {
        RepairPreset preset = findById(id);
        RepairPresetForm form = new RepairPresetForm();
        form.setId(preset.getId());
        form.setName(preset.getName());
        form.setPrice(preset.getPrice());
        return form;
    }

    /** 등록 또는 수정. */
    @Transactional
    public void save(RepairPresetForm form) {
        RepairPreset preset = (form.getId() == null) ? new RepairPreset() : findById(form.getId());
        preset.setName(form.getName());
        preset.setPrice(form.getPrice());
        repairPresetRepository.save(preset);
    }

    /** 삭제. 이미 접수 수리내역에서 사용된 프리셋은 삭제를 막는다. */
    @Transactional
    public void delete(Long id) {
        if (asRepairItemRepository.existsByPresetId(id)) {
            throw new IllegalStateException("이미 사용된 프리셋이라 삭제할 수 없습니다.");
        }
        repairPresetRepository.deleteById(id);
    }

    private RepairPreset findById(Long id) {
        return repairPresetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("프리셋을 찾을 수 없습니다."));
    }
}
