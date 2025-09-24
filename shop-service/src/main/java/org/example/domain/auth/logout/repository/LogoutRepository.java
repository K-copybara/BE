package org.example.domain.auth.logout.repository;

import org.example.domain.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LogoutRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByEmail(String email);
}

