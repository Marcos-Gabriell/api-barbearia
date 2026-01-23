package br.com.barbearia.apibarbearia.schedule.dtos;


import br.com.barbearia.apibarbearia.schedule.entity.enums.BlockType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BlockCreateDTO {
    public BlockType type;
    public LocalDate date;
    public LocalDateTime startAt;
    public LocalDateTime endAt;
    public String reason;
}
