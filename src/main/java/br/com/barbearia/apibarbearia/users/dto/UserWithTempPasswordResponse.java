package br.com.barbearia.apibarbearia.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserWithTempPasswordResponse {

    private UserResponse user;
    private String temporaryPassword;
}