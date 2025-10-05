package org.example.domain.stats.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.entity.OrderStatus;
import org.example.domain.entity.Orders;
import org.example.domain.order.repository.OrdersRepository;
import org.example.domain.stats.dto.response.DailySalesDto;
import org.example.domain.stats.dto.response.DailySalesResponse;
import org.example.domain.stats.dto.response.WeekdaySalesResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final OrdersRepository ordersRepository;

    // 월별 일별 매출
    @Transactional(readOnly = true)
    public List<DailySalesDto> getDailySales(Long storeId, String month) {
        String pattern = "order:stats:" + storeId + ":daily:" + month + "-*";

        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }

        List<DailySalesDto> result = new ArrayList<>();
        for (String key : keys) {
            Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
            Object salesValue = map.get("sales");

            if (salesValue != null) {
                // key 예시: order:stats:1:daily:2025-09-05
                String date = key.substring(key.length() - 10); // 마지막 10자리 = yyyy-MM-dd
                Long sales = Long.parseLong(salesValue.toString());
                result.add(new DailySalesDto(date, sales));
            }
        }

        // 날짜 오름차순 정렬
        return result.stream()
                .sorted(Comparator.comparing(DailySalesDto::getDate))
                .collect(Collectors.toList());
    }

    // 월별 요일별 매출
    @Transactional(readOnly = true)
    public List<WeekdaySalesResponse> getWeekdaySales(String month, Long storeId) {
        // 월 정보 파싱
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 해당 월의 완료된 주문 조회
        List<Orders> orders = ordersRepository.findByStoreIdAndOrderStatusAndCreatedAtBetween(
                storeId,
                OrderStatus.COMPLETED,
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay() // inclusive 보정
        );

        // 요일별 매출 합산
        Map<DayOfWeek, Long> weekdaySales = Arrays.stream(DayOfWeek.values())
                .collect(Collectors.toMap(day -> day, day -> 0L));

        for (Orders order : orders) {
            DayOfWeek dayOfWeek = order.getCreatedAt().getDayOfWeek();
            weekdaySales.put(dayOfWeek, weekdaySales.get(dayOfWeek) + order.getTotalPrice());
        }

        // 응답 변환
        return Arrays.stream(DayOfWeek.values())
                .map(day -> new WeekdaySalesResponse(day.name(), weekdaySales.get(day)))
                .toList();
    }

    // 일별 매출, 주문 건수
    @Transactional(readOnly = true)
    public DailySalesResponse getDailySales(String date, Long storeId) {
        // 요청 날짜 파싱
        LocalDate targetDate = LocalDate.parse(date);

        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.plusDays(1).atStartOfDay();

        // 해당 날짜의 완료된 주문 조회
        List<Orders> completedOrders = ordersRepository.findByStoreIdAndOrderStatusAndCreatedAtBetween(
                storeId,
                OrderStatus.COMPLETED,
                startOfDay,
                endOfDay
        );

        // 총 매출과 주문 건수 계산
        long totalSales = completedOrders.stream()
                .mapToLong(Orders::getTotalPrice)
                .sum();

        long orderCount = completedOrders.size();

        // 결과 반환
        return new DailySalesResponse(date, totalSales, orderCount);
    }
}
