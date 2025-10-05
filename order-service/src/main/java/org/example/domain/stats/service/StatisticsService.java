package org.example.domain.stats.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.stats.dto.request.DailySalesDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final RedisTemplate<String, Object> redisTemplate;

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
}
