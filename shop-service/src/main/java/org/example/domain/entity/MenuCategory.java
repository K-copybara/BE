package org.example.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Entity
@Table(name = "menu_category")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;

    @Column(name = "ordered_index", nullable = false)
    @Builder.Default
    private Long orderedIndex = 0L;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Menu> menus;

    public void changeOrder(Long order) {
        this.orderedIndex = order;
    }

    // 카테고리 삭제
    public void validateDeletable() {
        if (Boolean.TRUE.equals(this.isDefault)) {
            throw new IllegalStateException("기본 카테고리(요청사항)는 삭제할 수 없습니다.");
        }

        if (hasMenus()) {
            throw new IllegalStateException("해당 카테고리에 등록된 메뉴가 있어 삭제할 수 없습니다.");
        }
    }

    public boolean hasMenus() {
        return menus != null && !menus.isEmpty();
    }

}

