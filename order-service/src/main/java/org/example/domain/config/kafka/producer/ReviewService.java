package org.example.domain.config.kafka.producer;

import lombok.RequiredArgsConstructor;
import org.example.domain.config.kafka.dto.ReviewEvent;
import org.example.domain.entity.Orders;
import org.example.domain.order.dto.request.ReviewCreateRequest;
import org.example.domain.order.repository.OrdersRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrdersRepository ordersRepository;

    public void createReview(String orderId, ReviewCreateRequest request) {
        Orders order = ordersRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        for (ReviewCreateRequest.ReviewItem item : request.getReviews()) {
            ReviewEvent event = ReviewEvent.builder()
                    .storeId(order.getStoreId())
                    .orderId(orderId)
                    .menuId(item.getMenuId())
                    .score(item.getScore())
                    .customerKey(order.getCustomerKey())
                    .createdAt(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("review.create", event);
        }
    }
}

