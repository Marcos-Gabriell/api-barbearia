package br.com.barbearia.apibarbearia.catalog.controller;

import br.com.barbearia.apibarbearia.catalog.dto.*;
import br.com.barbearia.apibarbearia.catalog.service.CatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

        return ResponseEntity.ok(Map.of(
                "message", items.isEmpty()
                        ? "O catálogo está vazio. Nenhum serviço foi cadastrado ainda."
                        : "Catálogo listado com sucesso.",
                "data", items
        ));
    }

    // Opcional: lixeira (somente ADMIN/DEV já está no @PreAuthorize do controller)
    @GetMapping("/deleted")
    public ResponseEntity<?> listDeleted() {
        List<CatalogItemResponse> items = catalogService.listDeleted();
        return ResponseEntity.ok(Map.of(
                "message", "Serviços excluídos listados com sucesso.",
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

        return ResponseEntity.ok(Map.of(
                "message", "Serviço " + (toggled.isActive() ? "ativado" : "desativado") + " com sucesso.",
                "data", toggled
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Principal principal) {
        Long adminUserId = LoggedUser.userId(principal);
        String adminName = LoggedUser.name(principal);

        catalogService.delete(id, adminUserId, adminName);

        return ResponseEntity.ok(Map.of(
                "message", "Serviço excluído com sucesso."
        ));
    }

    static class LoggedUser {
        static Long userId(Principal principal) {
            // TODO: substituir por extração real do JWT (ex: SecurityContextHolder + claims)
            return 1L;
        }
        static String name(Principal principal) {
            return principal != null ? principal.getName() : "ADMIN";
        }
    }

    @GetMapping("/all-users")
    public ResponseEntity<?> listAllUsersForSelection() {
        List<UserMiniResponse> users = catalogService.listAllUsers();

        return ResponseEntity.ok(Map.of(
                "message", "Lista de usuários carregada com sucesso.",
                "data", users
        ));
    }
}
