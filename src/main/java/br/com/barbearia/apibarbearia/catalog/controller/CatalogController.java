package br.com.barbearia.apibarbearia.catalog.controller;

import br.com.barbearia.apibarbearia.catalog.dto.*;
import br.com.barbearia.apibarbearia.catalog.service.CatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity; // Importante
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/services")
@PreAuthorize("hasAnyRole('ADMIN','DEV')")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public ResponseEntity<?> listAll() {
        List<CatalogItemResponse> items = catalogService.listAll();

        if (items.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "message", "O catálogo está vazio. Nenhum serviço foi cadastrado ainda.",
                    "data", items // Retorna [] para o front não quebrar
            ));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Catálogo listado com sucesso.",
                "data", items
        ));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid CreateCatalogItemRequest request, Principal principal) {
        Long adminUserId = LoggedUser.userId(principal);
        String adminName = LoggedUser.name(principal);

        CatalogItemResponse created = catalogService.create(request, adminUserId, adminName);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Serviço adicionado ao catálogo com sucesso.",
                "data", created
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody @Valid UpdateCatalogItemRequest request, Principal principal) {
        Long adminUserId = LoggedUser.userId(principal);
        String adminName = LoggedUser.name(principal);

        CatalogItemResponse updated = catalogService.update(id, request, adminUserId, adminName);

        return ResponseEntity.ok(Map.of(
                "message", "Serviço atualizado com sucesso.",
                "data", updated
        ));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<?> toggleActive(@PathVariable Long id, Principal principal) {
        Long adminUserId = LoggedUser.userId(principal);
        String adminName = LoggedUser.name(principal);

        CatalogItemResponse toggled = catalogService.toggleActive(id, adminUserId, adminName);

        String statusMsg = toggled.isActive() ? "ativado" : "desativado";

        return ResponseEntity.ok(Map.of(
                "message", "Serviço " + statusMsg + " com sucesso.",
                "data", toggled
        ));
    }


    static class LoggedUser {
        static Long userId(Principal principal) {
            return 1L; // TODO: Implementar lógica real do JWT
        }
        static String name(Principal principal) {
            return principal != null ? principal.getName() : "ADMIN";
        }
    }
}