package com.minibank.exception;

import java.math.BigDecimal;

/**
 * Выбрасывается при попытке списания или перевода суммы,
 * превышающей доступный остаток на счете.
 */
public class InsufficientFundsException extends BankException {

    private final Long accountId;
    private final BigDecimal currentBalance;
    private final BigDecimal requestedAmount;

    public InsufficientFundsException(Long accountId, BigDecimal currentBalance, BigDecimal requestedAmount) {
        super(String.format("Недостаточно средств на счете #%d: текущий баланс %.2f, запрошено %.2f",
                accountId, currentBalance, requestedAmount));
        this.accountId = accountId;
        this.currentBalance = currentBalance;
        this.requestedAmount = requestedAmount;
    }

    public Long getAccountId() {
        return accountId;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }
}
