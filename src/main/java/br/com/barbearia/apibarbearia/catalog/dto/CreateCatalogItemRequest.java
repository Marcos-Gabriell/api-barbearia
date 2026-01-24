package br.com.barbearia.apibarbearia.catalog.dto;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class CreateCatalogItemRequest {

    @NotBlank(message = "Nome é obrigatório.")
    @Size(min = 3, max = 120, message = "Nome deve ter entre 3 e 120 caracteres.")
    private String name;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres.")
    private String description;

    @NotNull(message = "Duração é obrigatória.")
    @Min(value = 1, message = "Duração mínima é 1 minuto.")
    @Max(value = 480, message = "Duração máxima é 480 minutos.")
    private Integer durationMinutes;

    @NotNull(message = "Preço é obrigatório.")
    @DecimalMin(value = "0.00", inclusive = true, message = "Preço deve ser maior ou igual a zero.")
    @Digits(integer = 8, fraction = 2, message = "Preço inválido.")
    private BigDecimal price;

    /**
     * Opcional no create.
     * Se vier null, backend assume true.
     */
    private Boolean active;

    /**
     * IDs dos usuários que realizam o serviço.
     * Pelo menos 1 é obrigatório.
     */
    @NotNull(message = "Selecione pelo menos um responsável.")
    @Size(min = 1, message = "Selecione pelo menos um responsável.")
    private List<Long> responsibleUserIds;

    /* =======================
       Getters & Setters
       ======================= */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<Long> getResponsibleUserIds() {
        return responsibleUserIds;
    }

    public void setResponsibleUserIds(List<Long> responsibleUserIds) {
        this.responsibleUserIds = responsibleUserIds;
    }
}
