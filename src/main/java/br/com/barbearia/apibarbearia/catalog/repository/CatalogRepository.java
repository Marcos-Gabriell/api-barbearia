package br.com.barbearia.apibarbearia.catalog.repository;


import br.com.barbearia.apibarbearia.catalog.entity.CatalogItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogRepository extends JpaRepository<CatalogItem, Long> {
}
