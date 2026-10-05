package com.example.backend.health;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes a health-check endpoint used by the
 * frontend and Docker to verify that the backend is running.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * Returns the current status of the backend service.
     *
     * @return a map containing the key "status" with the value "UP"
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}