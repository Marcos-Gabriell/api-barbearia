package br.com.barbearia.apibarbearia.catalog.repository;

import br.com.barbearia.apibarbearia.catalog.entity.CatalogItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CatalogRepository extends JpaRepository<CatalogItem, Long> {

    List<CatalogItem> findAllByDeletedFalseOrderByNameAsc();

    List<CatalogItem> findAllByDeletedTrueOrderByUpdatedAtDesc();

    Optional<CatalogItem> findByIdAndDeletedFalse(Long id);

    boolean existsByNameIgnoreCaseAndDeletedFalse(String name);

    @Query("select (count(c) > 0) from CatalogItem c where lower(c.name) = lower(?1) and c.deleted = false and c.id <> ?2")
    boolean existsByNameIgnoreCaseAndDeletedFalseAndIdNot(String name, Long id);

    boolean existsByResponsibles_Id(Long userId);

    List<CatalogItem> findAllByActiveTrue();
}
