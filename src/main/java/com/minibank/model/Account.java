package com.minibank.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Доменная сущность банковского счета.
 * Инкапсулирует состояние счета и гарантирует валидность финансовых операций.
 * 
 * Внимание: для финансовой точности баланс хранится исключительно в BigDecimal.
 * Избегаем типа double/float из-за ошибок неточного представления в стандарте IEEE 754.
 */
public class Account {
    private final Long id;
    private final String accountNumber;
    private final String ownerName;
    private BigDecimal balance;
    private AccountStatus status;
    private final String currency;
    private final LocalDateTime createdAt;

    public Account(Long id, String accountNumber, String ownerName, BigDecimal initialBalance, String currency) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("Номер счета не может быть пустым");
        }
        if (ownerName == null || ownerName.isBlank()) {
            throw new IllegalArgumentException("Имя владельца счета не может быть пустым");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Валюта счета не может быть пустой");
        }

        this.id = id;
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = (initialBalance != null ? initialBalance : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        this.status = AccountStatus.ACTIVE;
        this.currency = currency.toUpperCase();
        this.createdAt = LocalDateTime.now();
    }

    // Геттеры
    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Мутирующие методы с проверками инвариантов бизнес-логики
    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма пополнения должна быть строго положительной");
        }
        this.balance = this.balance.add(amount).setScale(2, RoundingMode.HALF_UP);
    }

    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма списания должна быть строго положительной");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Недостаточно средств на счете");
        }
        this.balance = this.balance.subtract(amount).setScale(2, RoundingMode.HALF_UP);
    }

    public void block() {
        this.status = AccountStatus.BLOCKED;
    }

    public void activate() {
        this.status = AccountStatus.ACTIVE;
    }

    public void close() {
        if (this.balance.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Нельзя закрыть счет с ненулевым остатком: " + this.balance);
        }
        this.status = AccountStatus.CLOSED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account account)) return false;
        return Objects.equals(id, account.id) || Objects.equals(accountNumber, account.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accountNumber);
    }

    @Override
    public String toString() {
        return String.format("Account[id=%d, no='%s', owner='%s', balance=%.2f %s, status=%s]",
                id, accountNumber, ownerName, balance, currency, status.getDescription());
    }
}
