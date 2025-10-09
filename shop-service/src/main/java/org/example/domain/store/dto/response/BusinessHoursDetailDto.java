package org.example.domain.store.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BusinessHoursDetailDto {
    private final String dayOfWeek;
    private final String openTime;
    private final String closeTime;
    private final String breakOpenTime;
    private final String breakCloseTime;
}
