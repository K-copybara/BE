package org.example.domain.cart.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.cart.dto.request.CartItemRequest;
import org.example.domain.cart.dto.request.UpdateCartItemRequest;
import org.example.domain.cart.dto.response.AddToCartResponse;
import org.example.domain.cart.dto.response.CartItemResponse;
import org.example.domain.cart.dto.response.CartResponse;
import org.example.domain.cart.service.CartService;
import org.example.dto.Response;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // 장바구니 담기
    @PostMapping
    public Response<AddToCartResponse> addToCart(
            @RequestBody CartItemRequest request
    ) {
        AddToCartResponse response = cartService.addToCart(request);
        return Response.success("장바구니 담기 성공", response);
    }

    // 장바구니 조회
    @GetMapping
    public Response<CartResponse> getCart(
            @RequestParam Long storeId,
            @RequestParam String customerKey
    ) throws Exception {
        CartResponse response = cartService.getCart(storeId, customerKey);
        return Response.success("장바구니 조회 성공", response);
    }

    // 장바구니 수정 (수량 변경)
    @PatchMapping("/{cartItemId}")
    public Response<Void> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestBody UpdateCartItemRequest request
    ) {
        cartService.updateCartItem(cartItemId, request.getAmount());
        return Response.success("장바구니 수정 성공", null);
    }

    // 장바구니 아이템 삭제
    @DeleteMapping("/{cartItemId}")
    public Response<Void> deleteCartItem(
            @PathVariable Long cartItemId
    ) {
        cartService.deleteCartItem(cartItemId);
        return Response.success("장바구니 삭제 성공", null);
    }

}