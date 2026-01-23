package br.com.barbearia.apibarbearia.catalog.dto;

import javax.validation.constraints.*;
import java.math.BigDecimal;

public class UpdateCatalogItemRequest {

    @NotBlank
    @Size(max = 120)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull
    @Min(1)
    private Integer durationMinutes;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal price;

    public UpdateCatalogItemRequest() {}

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public BigDecimal getPrice() { return price; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
