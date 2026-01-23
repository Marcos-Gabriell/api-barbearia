package br.com.barbearia.apibarbearia.notification.email.template.catalog;


import br.com.barbearia.apibarbearia.catalog.events.CatalogChangedEvent;
import br.com.barbearia.apibarbearia.catalog.events.CatalogEventType;

public interface CatalogEmailTemplate {
    CatalogEventType type();
    String subject(CatalogChangedEvent event);
    String html(CatalogChangedEvent event);
}
