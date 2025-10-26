package org.example.domain.menu.repository;

import org.example.domain.entity.Menu;
import org.example.domain.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    @Query("SELECT m FROM Menu m JOIN FETCH m.category WHERE m.id IN :ids")
    List<Menu> findAllByIdWithCategory(@Param("ids") List<Long> ids);

    // 사장 메뉴 조회
    List<Menu> findByCategory_StoreOrderByCategory_OrderedIndexAscCreatedAtAsc(Store store);

    // 고객 메뉴 조회 (일시품절 제외)
    List<Menu> findByCategory_StoreAndMenuStatusTrueOrderByCategory_OrderedIndexAscCreatedAtAsc(Store store);
}
