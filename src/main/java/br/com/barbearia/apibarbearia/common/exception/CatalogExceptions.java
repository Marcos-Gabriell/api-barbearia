package br.com.barbearia.apibarbearia.common.exception;

public class CatalogExceptions {

    public static class CatalogItemNotFoundException extends RuntimeException {
        public CatalogItemNotFoundException(Long id) {
            super("Catalog item not found. id=" + id);
        }
    }

    public static class CatalogItemNameAlreadyExistsException extends RuntimeException {
        public CatalogItemNameAlreadyExistsException(String name) {
            super("Service name already exists: " + name);
        }
    }
}
