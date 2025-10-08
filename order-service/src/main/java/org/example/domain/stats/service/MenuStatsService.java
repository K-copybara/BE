package org.example.domain.stats.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.config.kafka.producer.OrderTop3Producer;
import org.example.domain.order.repository.OrderItemRepository;
import org.example.domain.order.repository.OrdersRepository;
import org.example.dto.Top3Menu.Top3MenuRequestEvent;
import org.example.dto.Top3Menu.Top3MenuResponseEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuStatsService {

    private final OrdersRepository ordersRepository;
    private final OrderTop3Producer orderTop3Producer;
    private final CompletableFutureService futureService;
    private final OrderItemRepository orderItemRepository;

    // 상위 3개
    public Top3MenuResponseEvent getTop3Menus(Long storeId) throws Exception {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        List<Long> top3MenuIds = ordersRepository.findTop3MenuIdsByStoreAndDate(storeId, oneMonthAgo);

        // Kafka 요청
        orderTop3Producer.sendTop3MenuRequest(storeId, top3MenuIds);

        // Kafka 응답 대기
        return futureService.waitForResponse(storeId, 3, TimeUnit.SECONDS);
    }

    // 특정 메뉴 판매량
    public Map<Long, Long> getMonthlySales(List<Long> menuIds) {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        List<Object[]> result = orderItemRepository.findMenuSalesByMenuIds(menuIds, oneMonthAgo);

        Map<Long, Long> salesMap = new HashMap<>();
        for (Object[] row : result) {
            Long menuId = (Long) row[0];
            Long totalSales = (Long) row[1];
            salesMap.put(menuId, totalSales);
        }

        return salesMap;
    }
}
