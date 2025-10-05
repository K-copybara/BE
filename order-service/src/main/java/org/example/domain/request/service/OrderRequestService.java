package org.example.domain.request.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.domain.entity.OrderRequest;
import org.example.domain.entity.OrderRequestItem;
import org.example.domain.request.dto.request.OrderRequestDto;
import org.example.domain.request.dto.response.OrderRequestResponse;
import org.example.domain.request.dto.response.OrderRequestMerchantResponse;
import org.example.domain.request.repository.OrderRequestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderRequestService {

    private final OrderRequestRepository orderRequestRepository;

    // 요청 생성
    @Transactional
    public OrderRequestResponse createOrderRequest(OrderRequestDto dto) {
        // 1. OrderRequest 생성
        OrderRequest orderRequest = OrderRequest.builder()
                .storeId(dto.getStoreId())
                .tableId(dto.getTableId())
                .customerKey(dto.getCustomerKey())
                .requestNote(dto.getRequestNote())
                .createdAt(LocalDateTime.now())
                .build();

        // 2. OrderRequestItem 생성
        if (dto.getItems() != null) {
            List<OrderRequestItem> items = dto.getItems().stream()
                    .map(i -> OrderRequestItem.builder()
                            .menuId(i.getMenuId())
                            .menuName(i.getMenuName())
                            .amount(i.getAmount())
                            .orderRequest(orderRequest)
                            .build())
                    .toList();
            orderRequest.getItems().addAll(items);
        }

        OrderRequest saved = orderRequestRepository.save(orderRequest);

        // 3. 응답 변환
        return OrderRequestResponse.fromEntity(saved);
    }

    // 사장 요청 조회
    public List<OrderRequestMerchantResponse> getRequestsByStore(Long storeId) {
        List<OrderRequest> requests = orderRequestRepository.findAllByStoreIdOrderByCreatedAtDesc(storeId);
        return requests.stream()
                .map(OrderRequestMerchantResponse::from)
                .toList();
    }

    // 사장 요청 완료
    @Transactional
    public void completeRequest(Long requestId) {
        OrderRequest request = orderRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수 없습니다."));

        request.complete();
    }
}

