package com.example.backend.item;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity that represents an item stored in the "items" table.
 */
@Entity
@Table(name = "items")
public class Item {

    /** Unique identifier of the item. */
    @Id
    private Long id;

    /** Name of the item (required). */
    @Column(nullable = false)
    private String name;

    /** Optional description of the item. */
    private String description;

    /**
     * Gets the item identifier.
     *
     * @return the item id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the item identifier.
     *
     * @param id the new item id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the item name.
     *
     * @return the item name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the item name.
     *
     * @param name the new item name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the item description.
     *
     * @return the item description, or null if not set
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the item description.
     *
     * @param description the new item description
     */
    public void setDescription(String description) {
        this.description = description;
    }
}