package org.example.domain.store.repository;


import org.example.domain.entity.MenuCategory;
import org.example.domain.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {
    List<MenuCategory> findByStoreOrderByOrderedIndexAsc(Store store);
}
