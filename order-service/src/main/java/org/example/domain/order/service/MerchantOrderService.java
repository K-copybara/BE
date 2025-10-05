package org.example.domain.order.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.domain.entity.OrderStatus;
import org.example.domain.entity.Orders;
import org.example.domain.order.dto.response.MerchantOrderItemDto;
import org.example.domain.order.dto.response.MerchantOrderSummaryDto;
import org.example.domain.order.repository.OrdersRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantOrderService {

    private final OrdersRepository ordersRepository;

    // 사장 주문 내역 조회
    @Transactional
    public List<MerchantOrderSummaryDto> getOrdersByStatus(Long storeId, OrderStatus status) {

        List<Orders> ordersList = ordersRepository.findOrdersByStoreAndStatus(storeId, status);

        return ordersList.stream()
                .map(order -> MerchantOrderSummaryDto.builder()
                        .orderId(order.getOrderId()) // UUID
                        .tableId(order.getTableId())
                        .orderedAt(order.getCreatedAt())
                        .status(order.getOrderStatus())
                        .requestNote(order.getRequestNote())
                        .items(order.getOrderItems().stream()
                                .map(item -> MerchantOrderItemDto.builder()
                                        .menuId(item.getMenuId())
                                        .menuName(item.getMenuName())
                                        .amount(item.getOrderQuantity())
                                        .build())
                                .toList())
                        .build())
                .toList();
    }

    // 주문 완료
    @Transactional
    public void completeOrder(String orderId) {
        Orders order = ordersRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if (order.getOrderStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("이미 완료된 주문입니다.");
        }

        order.complete();
    }
}

