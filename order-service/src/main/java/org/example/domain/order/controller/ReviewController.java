package org.example.domain.order.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.config.kafka.producer.ReviewService;
import org.example.domain.order.dto.request.ReviewCreateRequest;
import org.example.dto.Response;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer/orders")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/{orderId}/reviews")
    public Response<?> createReview(
            @PathVariable String orderId,
            @RequestBody ReviewCreateRequest request
    ) {
        reviewService.createReview(orderId, request);
        return Response.success("리뷰 작성 성공", null);
    }
}