package org.example.domain.stats.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.entity.OrderItem;
import org.example.domain.entity.OrderStatus;
import org.example.domain.entity.Orders;
import org.example.domain.order.repository.OrdersRepository;
import org.example.domain.stats.dto.response.*;
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

    private final RedisTemplate<String, String> redisTemplate;
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

    // 시간대별 매출 조회
    @Transactional(readOnly = true)
    public List<HourlySalesResponse> getHourlySales(String date, Long storeId) {
        // 날짜 파싱
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

        // 시간대별 초기화 (00~23)
        Map<Integer, HourlySalesResponse> hourlyStats = new LinkedHashMap<>();
        for (int hour = 0; hour < 24; hour++) {
            String hourLabel = String.format("%02d", hour);
            hourlyStats.put(hour, new HourlySalesResponse(hourLabel, 0L, 0L));
        }

        // 주문 데이터 집계
        for (Orders order : completedOrders) {
            int hour = order.getCreatedAt().getHour();
            HourlySalesResponse stat = hourlyStats.get(hour);

            long newSales = stat.getSales() + order.getTotalPrice();
            long newCount = stat.getOrderCount() + 1;

            hourlyStats.put(hour, new HourlySalesResponse(stat.getHour(), newSales, newCount));
        }

        // 결과 반환
        return new ArrayList<>(hourlyStats.values());
    }

    // 일별 메뉴 매출
    @Transactional(readOnly = true)
    public List<MenuSalesResponse> getMenuSales(String date, String sort, Long storeId) {
        LocalDate targetDate = LocalDate.parse(date);
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.plusDays(1).atStartOfDay();

        // 해당 날짜의 완료된 주문만 조회
        List<Orders> completedOrders = ordersRepository.findByStoreIdAndOrderStatusAndCreatedAtBetween(
                storeId, OrderStatus.COMPLETED, start, end);

        // 하루동안 메뉴별 매출, 주문건수 계산
        Map<Long, MenuSalesResponse> stats = new HashMap<>();

        for (Orders order : completedOrders) {   // 하루 동안 모든 주문 순회
            for (OrderItem item : order.getOrderItems()) {
                stats.compute(item.getMenuId(), (id, existing) -> { // menuId 존재하면 기존 값 업데이트, 없으면 새로 생성
                    if (existing == null) {   // 메뉴 처음 등장 -> 새로 추가
                        return MenuSalesResponse.builder()
                                .menuId(item.getMenuId())
                                .name(item.getMenuName())
                                .sales(item.getTotalMenuPrice())
                                .orderCount(1L)
                                .reviewCount(getReviewAvgFromRedis(item.getMenuId()))
                                .build();
                    } else {   // 이미 누적 중인 메뉴 -> 기존 값 더하기
                        return MenuSalesResponse.builder()
                                .menuId(id)
                                .name(existing.getName())
                                .sales(existing.getSales() + item.getTotalMenuPrice())
                                .orderCount(existing.getOrderCount() + 1)
                                .reviewCount(getReviewAvgFromRedis(item.getMenuId()))
                                .build();
                    }
                });
            }
        }

        // 정렬
        Comparator<MenuSalesResponse> comparator;
        if ("review".equalsIgnoreCase(sort)) {
            comparator = Comparator.comparing(MenuSalesResponse::getReviewCount).reversed();
        } else {
            comparator = Comparator.comparing(MenuSalesResponse::getSales).reversed();
        }

        return stats.values().stream()
                .sorted(comparator)
                .toList();
    }

    // Redis에서 리뷰 평점 가져오기
    private Long getReviewAvgFromRedis(Long menuId) {
        String key = "review:menu:" + menuId + ":avg";
        String value = redisTemplate.opsForValue().get(key);
        return (long) ((value != null) ? Double.parseDouble(value) : 0.0);
    }
}
