package com.nedko.bank.account.entity;

import com.nedko.bank.currency.entity.Currency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @JoinColumn(name = "currency_code", nullable = false)
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Currency currency;

    public Account(final BigDecimal balance, final Currency currency) {
        this.balance = balance;
        this.currency = currency;
    }

    public void deposit(final BigDecimal amount) {
         balance = balance.add(amount);
    }

    public void withdraw(final BigDecimal amount) {
         balance = balance.subtract(amount);
    }
}
