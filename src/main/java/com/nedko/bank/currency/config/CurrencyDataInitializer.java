package com.nedko.bank.currency.config;

import com.nedko.bank.currency.entity.Currency;
import com.nedko.bank.currency.repository.CurrencyRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrencyDataInitializer implements CommandLineRunner {

    private final CurrencyRepository repository;

    @Override
    public void run(final String... args) {
        repository.saveAll(List.of(
                new Currency("EUR", "Euro"),
                new Currency("USD", "United States dollar"),
                new Currency("GBP", "Pound sterling")));
    }
}
