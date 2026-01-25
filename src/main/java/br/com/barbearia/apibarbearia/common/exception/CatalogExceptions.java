package br.com.barbearia.apibarbearia.common.exception;

public class CatalogExceptions {

    public static class CatalogItemNotFoundException extends NotFoundException {
        public CatalogItemNotFoundException(Long id) {
            super("Serviço não encontrado. id=" + id);
        }
    }

    public static class CatalogItemNameAlreadyExistsException extends ConflictException {
        public CatalogItemNameAlreadyExistsException(String name) {
            super("Já existe um produto/serviço com esse nome.");
        }
    }
}
