package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestCreatedEvent {
    private Long requestId;
    private Long tableId;
    private LocalDateTime requestedAt;
    private String requestNote;
    private List<RequestItemDto> items;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class RequestItemDto {
        private String name;
        private int amount;
    }
}
