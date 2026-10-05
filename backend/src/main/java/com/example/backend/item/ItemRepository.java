package com.example.backend.item;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link Item} entities.
 * Provides CRUD operations using Long as the identifier type.
 */
public interface ItemRepository extends JpaRepository<Item, Long> {
}