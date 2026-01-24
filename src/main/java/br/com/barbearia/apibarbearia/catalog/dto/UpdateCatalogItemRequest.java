package br.com.barbearia.apibarbearia.catalog.dto;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class UpdateCatalogItemRequest {

    @NotBlank(message = "Nome é obrigatório.")
    @Size(min = 3, max = 120, message = "Nome deve ter entre 3 e 120 caracteres.")
    private String name;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres.")
    private String description;

    @NotNull(message = "Duração é obrigatória.")
    @Min(value = 1, message = "Duração deve ser >= 1.")
    private Integer durationMinutes;

    @NotNull(message = "Preço é obrigatório.")
    @DecimalMin(value = "0.00", inclusive = true, message = "Preço deve ser >= 0.")
    private BigDecimal price;

    @NotNull(message = "Active é obrigatório.")
    private Boolean active;

    @NotNull(message = "Selecione pelo menos 1 responsável.")
    @Size(min = 1, message = "Selecione pelo menos 1 responsável.")
    private List<Long> responsibleUserIds;

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public BigDecimal getPrice() { return price; }
    public Boolean getActive() { return active; }
    public List<Long> getResponsibleUserIds() { return responsibleUserIds; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setActive(Boolean active) { this.active = active; }
    public void setResponsibleUserIds(List<Long> responsibleUserIds) { this.responsibleUserIds = responsibleUserIds; }
}
