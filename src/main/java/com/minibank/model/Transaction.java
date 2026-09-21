package com.minibank.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Неизменяемая запись о финансовой транзакции (Ledger entry).
 * Все поля final: история транзакций не должна подвергаться изменениям.
 */
public class Transaction {
    private final Long id;
    private final LocalDateTime timestamp;
    private final BigDecimal amount;
    private final TransactionType type;
    private final TransactionCategory category;
    private final Long sourceAccountId;
    private final Long targetAccountId;
    private final String description;

    public Transaction(Long id, BigDecimal amount, TransactionType type,
                       TransactionCategory category, Long sourceAccountId,
                       Long targetAccountId, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма транзакции должна быть положительной");
        }
        this.id = id;
        this.timestamp = LocalDateTime.now();
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.type = Objects.requireNonNull(type, "Тип транзакции обязателен");
        this.category = Objects.requireNonNull(category, "Категория транзакции обязательна");
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.description = description != null ? description : "";
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public TransactionCategory getCategory() {
        return category;
    }

    public Long getSourceAccountId() {
        return sourceAccountId;
    }

    public Long getTargetAccountId() {
        return targetAccountId;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("[%s] #%d %s: %.2f (Категория: %s) | Откуда: %s -> Куда: %s | %s",
                timestamp.toLocalDate(), id, type.getTitle(), amount, category.getDisplayName(),
                sourceAccountId != null ? sourceAccountId : "Внешний источник",
                targetAccountId != null ? targetAccountId : "Внешний получатель",
                description);
    }
}
