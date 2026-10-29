package com.nedko.bank.account.controller;

import com.nedko.bank.account.dto.AccountDto;
import com.nedko.bank.account.dto.request.CreateAccountRequest;
import com.nedko.bank.account.dto.request.MoneyRequest;
import com.nedko.bank.account.dto.response.TransferResponse;
import com.nedko.bank.account.service.AccountService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes the HTTP API for account creation, lookup, deposits, and transfers.
 */
@RequestMapping("/accounts")
@RequiredArgsConstructor
@RestController
public class AccountController {

    private final AccountService service;

    /**
     * Creates an account with the requested currency and optional initial balance.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDto create(@RequestBody @Valid final CreateAccountRequest request) {
        return service.create(request.initialBalance(), request.currency());
    }

    /**
     * Returns the account identified by {@code id}.
     */
    @GetMapping("/{id}")
    public AccountDto get(@PathVariable final UUID id) {
         return service.find(id); 
    }

    /**
     * Deposits the requested amount into the account identified by {@code id}.
     */
    @PostMapping("/{id}/deposit")
    public AccountDto deposit(@PathVariable final UUID id,
            @RequestBody @Valid final MoneyRequest request) {
        return service.deposit(id, request.amount());
    }

    /**
     * Transfers the requested amount from {@code id} to {@code toId}.
     */
    @PostMapping("/{id}/transfer/{toId}")
    public TransferResponse transfer(@PathVariable final UUID id,
            @PathVariable final UUID toId,
            @RequestBody @Valid final MoneyRequest request) {
        return service.transfer(id, toId, request.amount());
    }
}
