package br.com.barbearia.apibarbearia.appointment.dto.response;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalSimpleResponse {
    private Long id;
    private String name;
}