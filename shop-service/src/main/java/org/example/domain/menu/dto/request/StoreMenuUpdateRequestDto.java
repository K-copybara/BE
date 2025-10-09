package org.example.domain.menu.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class StoreMenuUpdateRequestDto {
    private Long categoryId;
    private String name;
    private Long price;
    private String description;
    private Long spicyLevel;
    private List<String> allergies;
    private String extraInfo;
    private boolean removeImage; // true면 이미지 제거
}
