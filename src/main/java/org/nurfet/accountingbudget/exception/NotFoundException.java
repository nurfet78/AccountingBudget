package org.nurfet.accountingbudget.exception;

public class NotFoundException extends RuntimeException {

    public  <T> NotFoundException(final Class<T> cls, Long id) {
        super(cls.getSimpleName() + " с id " + id + " не найден");
    }
}
