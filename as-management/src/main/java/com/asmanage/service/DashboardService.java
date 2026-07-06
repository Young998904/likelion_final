package com.asmanage.service;

import com.asmanage.domain.AsRequest;
import com.asmanage.domain.AsStatus;
import com.asmanage.dto.DashboardData;
import com.asmanage.repository.AsRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        // 소요시간 분석: 완료 건의 접수→완료 처리시간(일)
        List<AsRequest> completedList = all.stream()
                .filter(r -> r.getStatus() == AsStatus.COMPLETED
                        && r.getCreatedAt() != null && r.getCompletedAt() != null)
                .toList();
        double avgProcessingDays = round1(completedList.stream()
                .mapToDouble(this::processingDays).average().orElse(0));

        // 제품별 평균 처리일수(오래 걸리는 순 정렬)
        Map<String, List<AsRequest>> byProduct = completedList.stream()
                .collect(Collectors.groupingBy(r -> r.getProduct().getName()));
        List<Map.Entry<String, Double>> productAvg = byProduct.entrySet().stream()
                .map(e -> Map.entry(e.getKey(),
                        round1(e.getValue().stream().mapToDouble(this::processingDays).average().orElse(0))))
                .sorted(Comparator.comparingDouble(Map.Entry<String, Double>::getValue).reversed())
                .toList();
        List<String> productNames = productAvg.stream().map(Map.Entry::getKey).toList();
        List<Double> avgDaysByProduct = productAvg.stream().map(Map.Entry::getValue).toList();

        return new DashboardData(total, received, repairing, awaitingPayment, awaitingDelivery,
                completed, unpaid, totalRevenue, months, receivedByMonth, completedByMonth,
                avgProcessingDays, productNames, avgDaysByProduct);
    }

    /** 접수→완료 처리시간(일). */
    private double processingDays(AsRequest request) {
        return Duration.between(request.getCreatedAt(), request.getCompletedAt()).toMinutes() / 1440.0;
    }

    /** 소수 첫째 자리 반올림. */
    private double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
