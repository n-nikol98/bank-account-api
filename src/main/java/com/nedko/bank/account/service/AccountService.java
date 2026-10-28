package com.nedko.bank.account.service;

import com.nedko.bank.account.dto.AccountDto;
import com.nedko.bank.account.dto.response.TransferResponse;
import com.nedko.bank.account.entity.Account;
import com.nedko.bank.account.exception.AccountNotFoundException;
import com.nedko.bank.account.exception.CurrencyMismatchException;
import com.nedko.bank.account.exception.InsufficientFundsException;
import com.nedko.bank.account.exception.SameAccountException;
import com.nedko.bank.account.mapper.AccountMapper;
import com.nedko.bank.account.repository.AccountRepository;
import com.nedko.bank.currency.entity.Currency;
import com.nedko.bank.currency.exception.CurrencyNotFoundException;
import com.nedko.bank.currency.repository.CurrencyRepository;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Coordinates account operations and maps persistence entities to API DTOs.
 */
@RequiredArgsConstructor
@Service
public class AccountService {

    private final AccountRepository repository;
    private final CurrencyRepository currencyRepository;
    private final AccountMapper mapper;

    /**
     * Creates and persists an account using the requested currency.
     */
    @Transactional
    public AccountDto create(final BigDecimal initialBalance, final String currencyCode) {
        final Currency currency = currencyRepository.findById(currencyCode)
                .orElseThrow(() -> new CurrencyNotFoundException(currencyCode));
        final Account account = repository.save(new Account(initialBalance == null ?
             BigDecimal.ZERO.setScale(2) : initialBalance, currency));
        return mapper.toDto(account);
    }

    /**
     * Finds an account without acquiring a write lock.
     */
    @Transactional(readOnly = true)
    public AccountDto find(final UUID id) {
        return mapper.toDto(repository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id)));
    }

    private Account findForUpdate(final UUID id) {
        return repository.findByIdForUpdate(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    /**
     * Deposits money into an account protected by a pessimistic write lock.
     */
    @Transactional
    public AccountDto deposit(final UUID id, final BigDecimal amount) {
        final Account account = findForUpdate(id);
        account.deposit(amount);
        return mapper.toDto(account);
    }

    /**
     * Transfers money between two accounts while locking them in deterministic
     * order to prevent deadlocks.
     */
    @Transactional
    public TransferResponse transfer(final UUID fromId, final UUID toId,
        final BigDecimal amount) {
        if (fromId.equals(toId)) {
            throw new SameAccountException(fromId, toId);
        }
        // Lock accounts in a deterministic order to prevent deadlocks when
        // concurrent transfers target the same accounts in opposite directions.
        final UUID firstId = fromId.compareTo(toId) < 0 ? fromId : toId;
        final UUID secondId = firstId.equals(fromId) ? toId : fromId;
        final Account first = findForUpdate(firstId);
        final Account second = findForUpdate(secondId);

        // Lock order is independent of transfer direction.
        final UUID firstAccountId = first.getId();
        final Account from = fromId.equals(firstAccountId) ? first : second;
        final Account to = toId.equals(firstAccountId) ? first : second;

        // Ensure that the from and to accounts are correctly identified.
        final Currency fromCurrency = from.getCurrency();
        final Currency toCurrency = to.getCurrency();
        if (!fromCurrency.equals(toCurrency)) {
            throw new CurrencyMismatchException(fromCurrency.getCode(),
                toCurrency.getCode());
        }
        final BigDecimal fromBalance = from.getBalance();
        if (fromBalance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(fromBalance, amount);
        }

        // Perform the transfer.
        from.withdraw(amount);
        to.deposit(amount);
        return new TransferResponse(amount, mapper.toDto(from), mapper.toDto(to));
    }
}
