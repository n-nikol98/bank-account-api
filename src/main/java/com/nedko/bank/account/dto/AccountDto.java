package com.nedko.bank.account.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountDto(UUID id, BigDecimal balance, String currency) {
}
