package com.nedko.bank.account.service;

import static com.nedko.bank.TestCurrencies.EUR;
import static com.nedko.bank.TestCurrencies.USD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nedko.bank.TestProfileResolver;
import com.nedko.bank.account.exception.AccountNotFoundException;
import com.nedko.bank.account.exception.CurrencyMismatchException;
import com.nedko.bank.account.exception.InsufficientFundsException;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;

@ActiveProfiles(resolver = TestProfileResolver.class)
@RequiredArgsConstructor
@SpringBootTest
@TestConstructor(autowireMode = AutowireMode.ALL)
class AccountServiceTest {

    private final AccountService service;

    @Test
    void transfersFundsBetweenAccounts() {
        final var from = service.create(new BigDecimal("12.50"), EUR);
        final UUID fromId = from.id();
        final var to = service.create(new BigDecimal("1.25"), EUR);
        final UUID toId = to.id();

        final BigDecimal transferAmount = new BigDecimal("3.40");
        final var result = service.transfer(fromId, toId, transferAmount);
        final var resultFrom = result.from();
        final var resultTo = result.to();

        assertThat(result.amount()).isEqualByComparingTo(transferAmount);
        assertThat(resultFrom.id()).isEqualTo(fromId);
        assertThat(resultFrom.balance()).isEqualByComparingTo("9.10");
        assertThat(resultFrom.currency()).isEqualTo(EUR);
        assertThat(resultTo.id()).isEqualTo(toId);
        assertThat(resultTo.balance()).isEqualByComparingTo("4.65");
        assertThat(resultTo.currency()).isEqualTo(EUR);
    }

    @Test
    void rejectsTransferWhenFundsAreInsufficient() {
        final var from = service.create(new BigDecimal("8.00"), EUR);
        final var to = service.create(new BigDecimal("2.00"), EUR);

        assertThatThrownBy(() -> service.transfer(from.id(), to.id(), new BigDecimal("8.01")))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void rejectsTransfersBetweenDifferentCurrencies() {
        final var from = service.create(new BigDecimal("15.00"), EUR);
        final var to = service.create(new BigDecimal("3.00"), USD);

        assertThatThrownBy(() -> service.transfer(from.id(), to.id(), new BigDecimal("4.00")))
                .isInstanceOf(CurrencyMismatchException.class);
    }

    @Test
    void missingAccountIsReported() {
        assertThatThrownBy(() -> service.find(UUID.randomUUID()))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
