package br.com.barbearia.apibarbearia.catalog.events;


import java.math.BigDecimal;

public class CatalogChangedEvent {

    private final CatalogEventType type;
    private final Long itemId;
    private final String name;
    private final Integer durationMinutes;
    private final BigDecimal price;
    private final boolean active;
    private final Long performedByUserId;
    private final String performedByName;

    public CatalogChangedEvent(CatalogEventType type,
                               Long itemId,
                               String name,
                               Integer durationMinutes,
                               BigDecimal price,
                               boolean active,
                               Long performedByUserId,
                               String performedByName) {
        this.type = type;
        this.itemId = itemId;
        this.name = name;
        this.durationMinutes = durationMinutes;
        this.price = price;
        this.active = active;
        this.performedByUserId = performedByUserId;
        this.performedByName = performedByName;
    }

    public CatalogEventType getType() { return type; }
    public Long getItemId() { return itemId; }
    public String getName() { return name; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public BigDecimal getPrice() { return price; }
    public boolean isActive() { return active; }
    public Long getPerformedByUserId() { return performedByUserId; }
    public String getPerformedByName() { return performedByName; }
}
