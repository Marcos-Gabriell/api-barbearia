package br.com.barbearia.apibarbearia.catalog.dto;

public class UserMiniResponse {
    private Long id;
    private String name;

    public UserMiniResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
}
