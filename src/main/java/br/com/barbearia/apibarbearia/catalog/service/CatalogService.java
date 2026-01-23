package br.com.barbearia.apibarbearia.catalog.service;

import br.com.barbearia.apibarbearia.catalog.dto.*;
import br.com.barbearia.apibarbearia.catalog.entity.CatalogItem;
import br.com.barbearia.apibarbearia.catalog.events.*;
import br.com.barbearia.apibarbearia.catalog.repository.CatalogRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogService {

    private final CatalogRepository repository;
    private final ApplicationEventPublisher publisher;

    public CatalogService(CatalogRepository repository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Transactional(readOnly = true)
    public List<CatalogItemResponse> listAll() {
        return repository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public CatalogItemResponse create(CreateCatalogItemRequest req, Long adminUserId, String adminName) {
        validate(req.getName(), req.getDurationMinutes(), req.getPrice());

        CatalogItem item = new CatalogItem(
                req.getName().trim(),
                normalize(req.getDescription()),
                req.getDurationMinutes(),
                req.getPrice(),
                adminUserId
        );

        CatalogItem saved = repository.save(item);

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
        validate(req.getName(), req.getDurationMinutes(), req.getPrice());

        CatalogItem item = repository.findById(id).orElseThrow(() -> new CatalogItemNotFoundException(id));

        item.update(
                req.getName().trim(),
                normalize(req.getDescription()),
                req.getDurationMinutes(),
                req.getPrice()
        );

        CatalogItem saved = repository.save(item);

        publisher.publishEvent(new CatalogChangedEvent(
                CatalogEventType.UPDATED,
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
    public CatalogItemResponse toggleActive(Long id, Long adminUserId, String adminName) {
        CatalogItem item = repository.findById(id).orElseThrow(() -> new CatalogItemNotFoundException(id));

        boolean wasActive = item.isActive();
        item.toggleActive();

        CatalogItem saved = repository.save(item);

        CatalogEventType type = wasActive ? CatalogEventType.DEACTIVATED : CatalogEventType.ACTIVATED;

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

    private CatalogItemResponse toResponse(CatalogItem item) {
        return new CatalogItemResponse(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getDurationMinutes(),
                item.getPrice(),
                item.isActive(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCreatedByUserId()
        );
    }

    private String normalize(String description) {
        if (description == null) return null;
        String d = description.trim();
        return d.isEmpty() ? null : d;
    }

    private void validate(String name, Integer durationMinutes, java.math.BigDecimal price) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Name is required");
        if (durationMinutes == null || durationMinutes < 1) throw new IllegalArgumentException("Duration must be >= 1");
        if (price == null || price.compareTo(java.math.BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Price must be >= 0");
    }

    public static class CatalogItemNotFoundException extends RuntimeException {
        public CatalogItemNotFoundException(Long id) {
            super("Catalog item not found. id=" + id);
        }
    }
}
