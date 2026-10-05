package com.example.backend.item;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes the items API under /api/items.
 */
@RestController
@RequestMapping("/api/items")
public class ItemController {

    /** Repository used to access the items stored in the database. */
    private final ItemRepository repository;

    /**
     * Creates the controller with its repository dependency.
     *
     * @param repository the JPA repository used to access items
     */
    public ItemController(ItemRepository repository) {
        this.repository = repository;
    }

    /**
     * Lists all items stored in the database.
     *
     * @return a list with every {@link Item} in the database
     */
    @GetMapping
    public List<Item> list() {
        return repository.findAll();
    }
}