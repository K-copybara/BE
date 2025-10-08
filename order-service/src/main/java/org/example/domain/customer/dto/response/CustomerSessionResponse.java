package org.example.domain.customer.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CustomerSessionResponse {
    private Long storeId;
    private Long tableId;
    private String customerKey;
    private LocalDateTime expiresAt;
}
