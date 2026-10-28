package com.nedko.bank.account.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record CreateAccountRequest(
        @DecimalMin(value = "0.00", inclusive = true)
        @Digits(integer = 17, fraction = 2)
        BigDecimal initialBalance,

        @NotBlank
        @Pattern(regexp = "[A-Z]{3}", message = "must be a three-letter uppercase currency code")
        String currency) {
}
