package br.com.barbearia.apibarbearia.catalog.controller;

import br.com.barbearia.apibarbearia.appointment.dto.response.ServiceSimpleResponse;
import br.com.barbearia.apibarbearia.catalog.dto.CatalogItemResponse;
import br.com.barbearia.apibarbearia.catalog.entity.CatalogItem;
import br.com.barbearia.apibarbearia.catalog.repository.CatalogRepository;
import br.com.barbearia.apibarbearia.catalog.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogPublicController {

    private final CatalogRepository catalogRepository;
    private final CatalogService catalogService;

    @GetMapping("/services")
    public ResponseEntity<List<CatalogItemResponse>> listActiveServices() {
        return ResponseEntity.ok(catalogService.listAll()
                .stream()
                .filter(CatalogItemResponse::isActive)
                .collect(Collectors.toList()));
    }
}