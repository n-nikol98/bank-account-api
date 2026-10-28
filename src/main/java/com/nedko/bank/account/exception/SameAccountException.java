package com.nedko.bank.account.exception;

import java.util.UUID;

public class SameAccountException extends RuntimeException {

    public SameAccountException(final UUID fromId, final UUID toId) {
        super("Source and destination accounts must be different: %s and %s."
                .formatted(fromId, toId));
    }
}
