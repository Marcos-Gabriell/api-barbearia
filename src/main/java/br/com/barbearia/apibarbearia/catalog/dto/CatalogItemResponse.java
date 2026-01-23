package br.com.barbearia.apibarbearia.catalog.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class CatalogItemResponse {

    private Long id;
    private String name;
    private String description;
    private Integer durationMinutes;
    private BigDecimal price;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private Long createdByUserId;

    public CatalogItemResponse() {}

    public CatalogItemResponse(Long id, String name, String description, Integer durationMinutes, BigDecimal price,
                               boolean active, Instant createdAt, Instant updatedAt, Long createdByUserId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.price = price;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdByUserId = createdByUserId;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public BigDecimal getPrice() { return price; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getCreatedByUserId() { return createdByUserId; }
}
