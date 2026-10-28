package com.nedko.bank.currency.exception;

public class CurrencyNotFoundException extends RuntimeException {

    public CurrencyNotFoundException(final String code) {
        super("Currency %s was not found.".formatted(code));
    }
}
