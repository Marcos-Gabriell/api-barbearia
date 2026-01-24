package br.com.barbearia.apibarbearia.catalog.service;

import br.com.barbearia.apibarbearia.catalog.dto.*;
import br.com.barbearia.apibarbearia.catalog.entity.CatalogItem;
import br.com.barbearia.apibarbearia.catalog.events.*;
import br.com.barbearia.apibarbearia.catalog.repository.CatalogRepository;
import br.com.barbearia.apibarbearia.common.exception.CatalogExceptions;
import br.com.barbearia.apibarbearia.users.entity.User;
import br.com.barbearia.apibarbearia.users.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CatalogService {

    private final CatalogRepository repository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;

    public CatalogService(CatalogRepository repository, UserRepository userRepository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.publisher = publisher;
    }

    @Transactional(readOnly = true)
    public List<CatalogItemResponse> listAll() {
        return repository.findAllByDeletedFalseOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CatalogItemResponse> listDeleted() {
        return repository.findAllByDeletedTrueOrderByUpdatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CatalogItemResponse create(CreateCatalogItemRequest req, Long adminUserId, String adminName) {
        String name = normalizeName(req.getName());
        String description = normalize(req.getDescription());
        Integer duration = req.getDurationMinutes();
        BigDecimal price = req.getPrice();

        // Validações
        validate(name, duration, price);
        validateResponsibles(req.getResponsibleUserIds());

        if (repository.existsByNameIgnoreCaseAndDeletedFalse(name)) {
            throw new CatalogExceptions.CatalogItemNameAlreadyExistsException(name);
        }

        Set<User> responsibles = loadResponsiblesOrFail(req.getResponsibleUserIds());

        CatalogItem item = new CatalogItem(
                name,
                description,
                duration,
                price,
                adminUserId
        );

        boolean active = req.getActive() == null || req.getActive();
        if (!active) item.toggleActive();

        item.setResponsibles(responsibles);

        CatalogItem saved = repository.save(item);

        // Publica evento simplificado (Create não precisa de diff de added/removed, pois todos são novos)
        publisher.publishEvent(new CatalogChangedEvent(
                CatalogEventType.CREATED,
                saved.getId(),
                saved.getName(),
                saved.getDurationMinutes(),
                saved.getPrice(),
                saved.isActive(),
                adminUserId,
                adminName
        ));

        return toResponse(saved);
    }

    @Transactional
    public CatalogItemResponse update(Long id, UpdateCatalogItemRequest req, Long adminUserId, String adminName) {
        CatalogItem item = repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CatalogExceptions.CatalogItemNotFoundException(id));

        // 1. Snapshot: IDs dos responsáveis ANTES da atualização
        Set<Long> oldResponsibleIds = item.getResponsibles().stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        String name = normalizeName(req.getName());
        String description = normalize(req.getDescription());
        Integer duration = req.getDurationMinutes();
        BigDecimal price = req.getPrice();

        validate(name, duration, price);

        if (req.getActive() == null) throw new IllegalArgumentException("O campo Ativo é obrigatório.");
        validateResponsibles(req.getResponsibleUserIds());

        if (repository.existsByNameIgnoreCaseAndDeletedFalseAndIdNot(name, id)) {
            throw new CatalogExceptions.CatalogItemNameAlreadyExistsException(name);
        }

        Set<User> responsibles = loadResponsiblesOrFail(req.getResponsibleUserIds());

        // Atualiza a entidade
        item.update(name, description, duration, price, req.getActive());
        item.setResponsibles(responsibles);

        CatalogItem saved = repository.save(item);

        // 2. Snapshot: IDs dos responsáveis DEPOIS da atualização
        Set<Long> newResponsibleIds = saved.getResponsibles().stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        // 3. Cálculo do DIFF (Quem entrou e quem saiu)
        Set<Long> addedIds = new HashSet<>(newResponsibleIds);
        addedIds.removeAll(oldResponsibleIds); // Presentes no novo, ausentes no antigo

        Set<Long> removedIds = new HashSet<>(oldResponsibleIds);
        removedIds.removeAll(newResponsibleIds); // Presentes no antigo, ausentes no novo

        // 4. Publica evento COMPLETO com os sets de diff
        publisher.publishEvent(new CatalogChangedEvent(
                CatalogEventType.UPDATED,
                saved.getId(),
                saved.getName(),
                saved.getDurationMinutes(),
                saved.getPrice(),
                saved.isActive(),
                adminUserId,
                adminName,
                addedIds,
                removedIds
        ));

        return toResponse(saved);
    }

    @Transactional
    public CatalogItemResponse toggleActive(Long id, Long adminUserId, String adminName) {
        CatalogItem item = repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CatalogExceptions.CatalogItemNotFoundException(id));

        boolean wasActive = item.isActive();
        item.toggleActive();

        CatalogItem saved = repository.save(item);

        CatalogEventType type = wasActive ? CatalogEventType.DEACTIVATED : CatalogEventType.ACTIVATED;

        // Publica evento simplificado
        publisher.publishEvent(new CatalogChangedEvent(
                type,
                saved.getId(),
                saved.getName(),
                saved.getDurationMinutes(),
                saved.getPrice(),
                saved.isActive(),
                adminUserId,
                adminName
        ));

        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id, Long adminUserId, String adminName) {
        CatalogItem item = repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CatalogExceptions.CatalogItemNotFoundException(id));

        // Captura dados para o evento antes de limpar
        Long itemId = item.getId();
        String itemName = item.getName();
        Integer duration = item.getDurationMinutes();
        BigDecimal price = item.getPrice();
        boolean isActive = item.isActive();

        // Remove vínculos de responsabilidade para limpar tabela de join
        item.setResponsibles(new HashSet<>());

        // Soft delete
        item.softDelete();
        repository.save(item);

        // Publica evento simplificado
        publisher.publishEvent(new CatalogChangedEvent(
                CatalogEventType.DELETED,
                itemId,
                itemName,
                duration,
                price,
                isActive,
                adminUserId,
                adminName
        ));
    }

    @Transactional(readOnly = true)
    public List<UserMiniResponse> listAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserMiniResponse(u.getId(), u.getName()))
                .sorted(Comparator.comparing(UserMiniResponse::getName))
                .collect(Collectors.toList());
    }

    // ---------------- helpers ----------------

    private CatalogItemResponse toResponse(CatalogItem item) {
        List<UserMiniResponse> responsibles = item.getResponsibles()
                .stream()
                .sorted(Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER))
                .map(u -> new UserMiniResponse(u.getId(), u.getName()))
                .collect(Collectors.toList());

        String createdByName = "Sistema";
        if (item.getCreatedByUserId() != null) {
            createdByName = userRepository.findById(item.getCreatedByUserId())
                    .map(User::getName)
                    .orElse("Desconhecido");
        }

        return new CatalogItemResponse(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getDurationMinutes(),
                item.getPrice(),
                item.isActive(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCreatedByUserId(),
                createdByName,
                responsibles
        );
    }

    private Set<User> loadResponsiblesOrFail(List<Long> ids) {
        if (ids == null) return new HashSet<>();

        List<Long> uniqueIds = ids.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<User> users = userRepository.findAllById(uniqueIds);

        if (users.size() != uniqueIds.size()) {
            throw new IllegalArgumentException("Um ou mais responsáveis selecionados não foram encontrados.");
        }
        return new HashSet<>(users);
    }

    private void validateResponsibles(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Selecione pelo menos 1 responsável.");
        }
    }

    private String normalize(String description) {
        if (description == null) return null;
        String d = description.trim();
        if (d.length() > 100) return d.substring(0, 100);
        return d.isEmpty() ? null : d;
    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim();
    }

    private void validate(String name, Integer durationMinutes, BigDecimal price) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("O nome do serviço é obrigatório.");
        }
        if (name.length() < 3 || name.length() > 100) {
            throw new IllegalArgumentException("O nome deve ter entre 3 e 100 caracteres.");
        }
        if (durationMinutes == null || durationMinutes < 5) {
            throw new IllegalArgumentException("A duração mínima deve ser de 5 minutos.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O preço não pode ser negativo.");
        }
    }
}