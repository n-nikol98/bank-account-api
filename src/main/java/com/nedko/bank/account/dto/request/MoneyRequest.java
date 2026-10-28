package com.nedko.bank.account.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record MoneyRequest(
        @NotNull
        @DecimalMin(value = "0.01", inclusive = true)
        @Digits(integer = 17, fraction = 2)
        BigDecimal amount) {
}
