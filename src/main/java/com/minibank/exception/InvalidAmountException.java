package com.minibank.exception;

import java.math.BigDecimal;

/**
 * Выбрасывается при передаче некорректной суммы транзакции (ноль, отрицательное значение или null).
 */
public class InvalidAmountException extends BankException {

    private final BigDecimal amount;

    public InvalidAmountException(BigDecimal amount) {
        super(String.format("Некорректная сумма транзакции: %s. Сумма должна быть строго положительной.",
                amount != null ? amount.toPlainString() : "null"));
        this.amount = amount;
    }

    public InvalidAmountException(String message) {
        super(message);
        this.amount = null;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
