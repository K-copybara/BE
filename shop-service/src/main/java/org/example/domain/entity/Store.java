package org.example.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.domain.store.dto.response.BusinessHoursDetailDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "store")
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_name", nullable = false, length = 100)
    private String shopName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false)
    @Builder.Default
    private Boolean status = true;  // OPEN: true, CLOSED: false

    @Column(columnDefinition = "TEXT")
    private String notice;

    @Column(name = "business_hours", columnDefinition = "TEXT")
    private String businessHours;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuCategory> categories;

    public void deactivate() {
        this.status = false;
    }

    // 요청사항 카테고리는 모든 상점에 기본 적용
    @PostPersist
    public void createDefaultCategory() {
        // store 저장 직후 호출됨 (JPA lifecycle callback)
        if (this.categories == null) {
            this.categories = new ArrayList<>();
        }

        MenuCategory defaultCategory = MenuCategory.builder()
                .store(this)
                .categoryName("요청사항")
                .orderedIndex(0L)
                .isDefault(true)
                .build();

        this.categories.add(defaultCategory);
    }

    // 상점 공지 수정
    public void changeNotice(String newNotice) {
        if (newNotice == null || newNotice.isBlank()) {
            throw new IllegalArgumentException("공지 내용은 비워둘 수 없습니다.");
        }
        this.notice = newNotice;
        this.updatedAt = LocalDateTime.now();
    }

    // 영업시간 변경
    public void changeBusinessHours(List<BusinessHoursDetailDto> hours) {
        if (hours == null || hours.isEmpty()) {
            this.businessHours = null;
            this.updatedAt = LocalDateTime.now();
            return;
        }

        this.businessHours = hours.stream()
                .filter(Objects::nonNull)
                .map(dto -> String.format("%s:%s,%s,%s,%s",
                        dto.getDayOfWeek() != null ? dto.getDayOfWeek() : "",
                        dto.getOpenTime() != null ? dto.getOpenTime() : "",
                        dto.getCloseTime() != null ? dto.getCloseTime() : "",
                        dto.getBreakOpenTime() != null ? dto.getBreakOpenTime() : "",
                        dto.getBreakCloseTime() != null ? dto.getBreakCloseTime() : ""
                ))
                .collect(Collectors.joining(";"));

        this.updatedAt = LocalDateTime.now();
    }

    public void validateOwnership(MenuCategory category) {
        if (!category.getStore().getId().equals(this.id)) {
            throw new IllegalStateException("다른 상점의 카테고리는 삭제할 수 없습니다.");
        }
    }
}
