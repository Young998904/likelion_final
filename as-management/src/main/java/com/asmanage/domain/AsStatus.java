package com.asmanage.domain;

import lombok.Getter;

/**
 * A/S 접수 건의 진행 상태.
 * 접수 → 수리중 → 입금대기 → 배송대기 → 완료 순으로만 진행한다(역방향 없음).
 */
@Getter
public enum AsStatus {
    RECEIVED("접수"),
    REPAIRING("수리중"),
    AWAITING_PAYMENT("입금대기"),
    AWAITING_DELIVERY("배송대기"),
    COMPLETED("완료");

    // 화면에 표시할 한글 상태명
    private final String label;

    AsStatus(String label) {
        this.label = label;
    }
}
