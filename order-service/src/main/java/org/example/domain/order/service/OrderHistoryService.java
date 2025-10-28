package org.example.domain.order.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.domain.config.CustomerSessionValidator;
import org.example.domain.entity.OrderRequest;
import org.example.domain.entity.Orders;
import org.example.domain.order.dto.response.ItemDto;
import org.example.domain.order.dto.response.OrderHistoryResponse;
import org.example.domain.order.dto.response.OrderSummary;
import org.example.domain.order.repository.OrdersRepository;
import org.example.domain.request.repository.OrderRequestRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OrderHistoryService {

    private final OrdersRepository ordersRepository;
    private final OrderRequestRepository orderRequestRepository;
    private final CustomerSessionValidator sessionValidator;
    private final RedisTemplate<String, String> redisTemplate;

    //  고객 주문 내역 조회
    @Transactional
    public OrderHistoryResponse getCustomerOrders(Long storeId, String customerKey) {

        // 고객 검증
        sessionValidator.validate(customerKey);

        // 주문 (결제한 메뉴 주문) 조회
        List<Orders> ordersList = ordersRepository.findOrdersWithItemsByCustomer(storeId, customerKey);

        // 요청사항 주문(결제 없는 주문) 조회
        List<OrderRequest> requestList = orderRequestRepository.findByStoreIdAndCustomerKey(storeId, customerKey);
        // Orders → OrderSummary 변환
        List<OrderSummary> orderSummaries = ordersList.stream()
                .map(order -> OrderSummary.builder()
                        .orderId(order.getOrderId())
                        .totalPrice(order.getTotalPrice())
                        .requestNote(order.getRequestNote())
                        .status(order.getOrderStatus())
                        .createdAt(order.getCreatedAt())
                        .reviewed("true".equals(redisTemplate.opsForValue().get("reviewed:" + order.getOrderId())))
                        .items(order.getOrderItems().stream()
                                .map(item -> ItemDto.builder()
                                        .menuId(item.getMenuId())
                                        .menuName(item.getMenuName())
                                        .amount(item.getOrderQuantity())
                                        .price(item.getMenuPrice())
                                        .totalPrice(item.getTotalMenuPrice())
                                        .build())
                                .toList())
                        .build())
                .toList();

        // OrderRequest → OrderSummary 변환
        List<OrderSummary> requestSummaries = requestList.stream()
                .map(req -> OrderSummary.builder()
                        .orderId(String.valueOf(req.getId())) // string 변환
                        .totalPrice(0L) // 요청사항은 결제 없음
                        .requestNote(req.getRequestNote())
                        .status(req.getRequestStatus())
                        .createdAt(req.getCreatedAt())
                        .items(req.getItems().stream()
                                .map(item -> ItemDto.builder()
                                        .menuId(item.getMenuId())
                                        .menuName(item.getMenuName())
                                        .amount(item.getAmount())
                                        .price(0L) // 요청사항은 가격 없음
                                        .totalPrice(0L)
                                        .build())
                                .toList())
                        .build())
                .toList();

        //  두 리스트 합치고 최신순 정렬
        List<OrderSummary> combined = Stream.concat(orderSummaries.stream(), requestSummaries.stream())
                .sorted(Comparator.comparing(OrderSummary::getCreatedAt).reversed())
                .toList();

        // 총 지출 금액 계산
        Long totalSpent = ordersList.stream()
                .mapToLong(o -> Optional.ofNullable(o.getTotalPrice()).orElse(0L))
                .sum();

        // 응답 조립
        return OrderHistoryResponse.builder()
                .storeId(storeId)
                .totalOrders((long) combined.size())
                .totalSpent(totalSpent)
                .orders(combined)
                .build();
    }
}
