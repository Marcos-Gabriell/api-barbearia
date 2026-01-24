package br.com.barbearia.apibarbearia.catalog.events;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

public class CatalogChangedEvent {

    private final CatalogEventType type;
    private final Long itemId;
    private final String itemName;
    private final Integer durationMinutes;
    private final BigDecimal price;
    private final boolean active;
    private final Long adminUserId;
    private final String adminName;

    // NOVOS CAMPOS: Diff de responsáveis
    private final Set<Long> addedResponsibleIds;
    private final Set<Long> removedResponsibleIds;

    // Construtor Completo (Usado no Update)
    public CatalogChangedEvent(CatalogEventType type, Long itemId, String itemName,
                               Integer durationMinutes, BigDecimal price, boolean active,
                               Long adminUserId, String adminName,
                               Set<Long> addedResponsibleIds, Set<Long> removedResponsibleIds) {
        this.type = type;
        this.itemId = itemId;
        this.itemName = itemName;
        this.durationMinutes = durationMinutes;
        this.price = price;
        this.active = active;
        this.adminUserId = adminUserId;
        this.adminName = adminName;
        this.addedResponsibleIds = addedResponsibleIds != null ? addedResponsibleIds : Collections.emptySet();
        this.removedResponsibleIds = removedResponsibleIds != null ? removedResponsibleIds : Collections.emptySet();
    }

    // Construtor Simplificado (Usado em Create, Delete, Toggle - onde não calculamos diff)
    public CatalogChangedEvent(CatalogEventType type, Long itemId, String itemName,
                               Integer durationMinutes, BigDecimal price, boolean active,
                               Long adminUserId, String adminName) {
        this(type, itemId, itemName, durationMinutes, price, active, adminUserId, adminName, Collections.emptySet(), Collections.emptySet());
    }

    public CatalogEventType getType() { return type; }
    public Long getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public BigDecimal getPrice() { return price; }
    public boolean isActive() { return active; }
    public Long getAdminUserId() { return adminUserId; }
    public String getAdminName() { return adminName; }

    public Set<Long> getAddedResponsibleIds() { return addedResponsibleIds; }
    public Set<Long> getRemovedResponsibleIds() { return removedResponsibleIds; }
}