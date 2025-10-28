package org.example.domain.config.kafka.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewEvent {
    private Long storeId;
    private String orderId;
    private Long menuId;
    private Long score;
    private String customerKey;
    private LocalDateTime createdAt;
}