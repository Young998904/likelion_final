package com.asmanage.service;

import com.asmanage.domain.AsRequest;
import com.asmanage.domain.AsStatus;
import com.asmanage.dto.DashboardData;
import com.asmanage.repository.AsRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * 대시보드 요약 통계 및 최근 6개월 추이를 계산한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final int TREND_MONTHS = 6;

    private final AsRequestRepository asRequestRepository;

    public DashboardData load() {
        long total = asRequestRepository.count();
        long received = asRequestRepository.countByStatus(AsStatus.RECEIVED);
        long repairing = asRequestRepository.countByStatus(AsStatus.REPAIRING);
        long awaitingPayment = asRequestRepository.countByStatus(AsStatus.AWAITING_PAYMENT);
        long awaitingDelivery = asRequestRepository.countByStatus(AsStatus.AWAITING_DELIVERY);
        long completed = asRequestRepository.countByStatus(AsStatus.COMPLETED);
        long unpaid = asRequestRepository.countByPaidFalse();

        // 완료 건의 청구 금액 합계(매출)
        long totalRevenue = asRequestRepository.findByStatus(AsStatus.COMPLETED).stream()
                .mapToLong(r -> r.getTotalAmount())
                .sum();

        // 최근 6개월 접수/완료 추이
        List<AsRequest> all = asRequestRepository.findAll();
        List<String> months = new ArrayList<>();
        List<Long> receivedByMonth = new ArrayList<>();
        List<Long> completedByMonth = new ArrayList<>();

        YearMonth current = YearMonth.now();
        for (int i = TREND_MONTHS - 1; i >= 0; i--) {
            YearMonth month = current.minusMonths(i);
            months.add(month.toString());
            receivedByMonth.add(all.stream()
                    .filter(r -> r.getCreatedAt() != null && YearMonth.from(r.getCreatedAt()).equals(month))
                    .count());
            completedByMonth.add(all.stream()
                    .filter(r -> r.getCompletedAt() != null && YearMonth.from(r.getCompletedAt()).equals(month))
                    .count());
        }

        return new DashboardData(total, received, repairing, awaitingPayment, awaitingDelivery,
                completed, unpaid, totalRevenue, months, receivedByMonth, completedByMonth);
    }
}
