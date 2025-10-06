package org.example.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private Long storeId;
    private Long tableId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime requestedAt;

    private String requestNote;
    private List<RequestItemDto> items;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class RequestItemDto {
        private String name;
        private Long amount;
    }
}