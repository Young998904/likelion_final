package com.asmanage.dto;

import java.util.List;

/**
 * 대시보드 요약 통계 및 차트용 데이터.
 */
public record DashboardData(
        long total,
        long received,
        long repairing,
        long awaitingPayment,
        long awaitingDelivery,
        long completed,
        long unpaid,
        long totalRevenue,
        // 최근 6개월 라벨(예: "2026-07")과 월별 접수/완료 건수
        List<String> months,
        List<Long> receivedByMonth,
        List<Long> completedByMonth
) {
}
