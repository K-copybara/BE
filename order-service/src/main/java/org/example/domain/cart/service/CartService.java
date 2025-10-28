package org.example.domain.cart.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.cart.dto.request.CartItemRequest;
import org.example.domain.cart.dto.response.AddToCartResponse;
import org.example.domain.cart.dto.response.CartItemResponse;
import org.example.domain.cart.dto.response.CartResponse;
import org.example.domain.cart.repository.CartItemRepository;
import org.example.domain.cart.repository.CartRepository;
import org.example.domain.config.CustomerSessionValidator;
import org.example.domain.config.kafka.consumer.CartMenuResponseConsumer;
import org.example.domain.config.kafka.consumer.MenuResponseConsumer;
import org.example.domain.config.kafka.dto.CartMenuDto;
import org.example.domain.config.kafka.dto.MenuDto;
import org.example.domain.config.kafka.producer.CartMenuRequestProducer;
import org.example.domain.config.kafka.producer.MenuRequestProducer;
import org.example.domain.entity.Cart;
import org.example.domain.entity.CartItem;
import org.example.domain.entity.CartStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartMenuRequestProducer cartMenuRequestProducer;
    private final CartMenuResponseConsumer cartMenuResponseConsumer;
    private final CustomerSessionValidator sessionValidator;

    // 장바구니 담기
    @Transactional
    public AddToCartResponse addToCart(CartItemRequest request) {
        // 고객 검증
        sessionValidator.validate(request.getCustomerKey());

        // 1. Cart 조회 or 생성
        Cart cart = findOrCreateCart(request.getStoreId(), request.getCustomerKey());

        // 2. 기존 CartItem 조회
        Optional<CartItem> existingItemOpt = cartItemRepository
                .findByCartAndMenuId(cart, request.getMenuId());

        CartItem saved;

        if (existingItemOpt.isPresent()) {
            // 이미 같은 메뉴가 존재 → 수량 증가
            CartItem existingItem = existingItemOpt.get();
            long newQuantity = existingItem.getQuantity() + request.getAmount();
            existingItem.updateQuantity(newQuantity);
            saved = cartItemRepository.save(existingItem);
        } else {
            // 새로운 메뉴 추가
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .menuId(request.getMenuId())
                    .quantity((long) request.getAmount())
                    .build();
            saved = cartItemRepository.save(newItem);
        }

        // 3. 응답 변환 (Kafka 호출 없음)
        return AddToCartResponse.fromEntity(saved);
    }

    // 장바구니 조회
    @Transactional
    public CartResponse getCart(Long storeId, String customerKey) throws Exception {

        // 고객 검증
        sessionValidator.validate(customerKey);

        // ACTIVE 카트 조회 (없으면 새로 생성)
        Cart cart = cartRepository.findByStoreIdAndCustomerKeyAndStatus(storeId, customerKey, CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Cart newCart = Cart.newActiveCart(storeId, customerKey);
                    log.info("ACTIVE 카트 없음 → 새로 생성: storeId={}, customerKey={}", storeId, customerKey);
                    return cartRepository.save(newCart);
                });

        List<Long> menuIds = cart.getItems().stream()
                .map(CartItem::getMenuId)
                .toList();

        if (menuIds.isEmpty()) {
            return CartResponse.of(cart, List.of());
        }

        // Kafka 요청
        String requestId = UUID.randomUUID().toString();
        cartMenuRequestProducer.sendMenuRequest(requestId, menuIds);

        // Kafka 응답 대기
        CompletableFuture<List<CartMenuDto>> future = cartMenuResponseConsumer.registerRequest(requestId);
        List<CartMenuDto> menus = future.get(3, TimeUnit.SECONDS);

        // Response 조립
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> {
                    CartMenuDto menuDto = menus.stream()
                            .filter(m -> m.getMenuId().equals(item.getMenuId()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("메뉴 정보를 찾을 수 없습니다. (menuId=" + item.getMenuId() + ")"));
                    return CartItemResponse.builder()
                            .cartItemId(item.getId())
                            .menuId(menuDto.getMenuId())
                            .menuName(menuDto.getMenuName())
                            .menuCategory(menuDto.getMenuCategory())
                            .menuPicture(menuDto.getMenuPicture())
                            .amount(item.getQuantity())
                            .price(menuDto.getMenuPrice())
                            .build();
                })
                .toList();

        return CartResponse.of(cart, itemResponses);
    }

    // 장바구니 조회 / 생성 (공통 로직)
    private Cart findOrCreateCart(Long storeId, String customerKey) {

        // 고객 검증
        sessionValidator.validate(customerKey);

        return cartRepository.findByStoreIdAndCustomerKeyAndStatus(storeId, customerKey, CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Cart newCart = Cart.newActiveCart(storeId, customerKey);
                    return cartRepository.save(newCart);
                });
    }

    // 장바구니 아이템 수량 수정
    @Transactional
    public void updateCartItem(Long cartItemId, int newAmount) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다."));

        String customerKey = cartItem.getCart().getCustomerKey();
        sessionValidator.validate(customerKey);

        cartItem.updateQuantity((long) newAmount);
        cartItemRepository.save(cartItem);
    }

    // 장바구니 아이템 삭제
    @Transactional
    public void deleteCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다."));

        String customerKey = cartItem.getCart().getCustomerKey();
        sessionValidator.validate(customerKey);

        cartItemRepository.delete(cartItem);
    }

}
