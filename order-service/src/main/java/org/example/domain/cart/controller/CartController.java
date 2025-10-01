package org.example.domain.cart.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.cart.dto.request.CartItemRequest;
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

    @PostMapping
    public Response<AddToCartResponse> addToCart(
            @RequestBody CartItemRequest request
    ) {
        AddToCartResponse response = cartService.addToCart(request);
        return Response.success("장바구니 담기 성공", response);
    }

    @GetMapping
    public Response<CartResponse> getCart(
            @RequestParam Long storeId,
            @RequestParam String customerKey
    ) throws Exception {
        CartResponse response = cartService.getCart(storeId, customerKey);
        return Response.success("장바구니 조회 성공", response);
    }
}