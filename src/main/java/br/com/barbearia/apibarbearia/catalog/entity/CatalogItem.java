package br.com.barbearia.apibarbearia.catalog.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "catalog_items")
public class CatalogItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(nullable = false, updatable = false)
    private Long createdByUserId;

    protected CatalogItem() {}

    public CatalogItem(String name, String description, Integer durationMinutes, BigDecimal price, Long createdByUserId) {
        this.name = name;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.price = price;
        this.createdByUserId = createdByUserId;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public void update(String name, String description, Integer durationMinutes, BigDecimal price) {
        this.name = name;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.price = price;
    }

    public void toggleActive() {
        this.active = !this.active;
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
