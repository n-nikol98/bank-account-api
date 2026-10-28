package com.nedko.bank.account.dto.response;

import com.nedko.bank.account.dto.AccountDto;
import java.math.BigDecimal;

public record TransferResponse(
        BigDecimal amount,
        AccountDto from,
        AccountDto to) {
}
