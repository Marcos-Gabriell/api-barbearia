package br.com.barbearia.apibarbearia.catalog.entity;

import br.com.barbearia.apibarbearia.users.entity.User;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

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

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(nullable = false, updatable = false)
    private Long createdByUserId;

    // ✅ CORREÇÃO: FetchType.EAGER garante que a lista venha preenchida
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "catalog_item_responsibles",
            joinColumns = @JoinColumn(name = "catalog_item_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> responsibles = new HashSet<>();

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

    public void update(String name, String description, Integer durationMinutes, BigDecimal price, boolean active) {
        this.name = name;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.price = price;
        this.active = active;
    }

    public void toggleActive() {
        this.active = !this.active;
    }

    public void softDelete() {
        this.deleted = true;
        this.active = false;
    }

    public void setResponsibles(Set<User> users) {
        this.responsibles = users;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public BigDecimal getPrice() { return price; }
    public boolean isActive() { return active; }
    public boolean isDeleted() { return deleted; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getCreatedByUserId() { return createdByUserId; }
    public Set<User> getResponsibles() { return responsibles; }
}