package org.example.domain.store.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.domain.store.dto.response.BusinessHoursDetailDto;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoreBusinessHoursUpdateRequestDto {
    private List<BusinessHoursDetailDto> businessHours;
}
