package org.example.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "menu")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private MenuCategory category;

    @Column(name = "menu_name", nullable = false, length = 50)
    private String menuName;

    @Column(name = "menu_price", nullable = false)
    private Long menuPrice;

    @Column(name = "menu_info", nullable = false, length = 255)
    private String menuInfo;

    @Column(name = "menu_picture", columnDefinition = "TEXT", nullable = false)
    private String menuPicture;

    @Column(name = "menu_status", nullable = false)
    @Builder.Default
    private Boolean menuStatus = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long spicy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String allergy;

    @Column(name = "extra_info", columnDefinition = "TEXT")
    private String extraInfo;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings;
}

