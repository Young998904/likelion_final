package com.asmanage.domain;

import lombok.Getter;

/**
 * 알림 로그 유형(실제 SMS 대신 화면/DB에 기록하는 통지 이력).
 */
@Getter
public enum NotificationType {
    RECEIPT("접수"),
    PAYMENT_REQUEST("입금요청"),
    DELIVERY("배송");

    // 화면에 표시할 한글 유형명
    private final String label;

    NotificationType(String label) {
        this.label = label;
    }
}
