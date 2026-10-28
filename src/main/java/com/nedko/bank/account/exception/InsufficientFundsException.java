package com.nedko.bank.account.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(final BigDecimal balance, final BigDecimal amount) {
        super("Insufficient funds: available balance is %s, requested amount is %s."
                .formatted(balance, amount));
    }
}
