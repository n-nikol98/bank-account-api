package com.nedko.bank.account.exception;

public class CurrencyMismatchException extends RuntimeException {

    public CurrencyMismatchException(final String fromCurrency, final String toCurrency) {
        super("Transfers between different currencies are not supported: %s and %s."
                .formatted(fromCurrency, toCurrency));
    }
}
