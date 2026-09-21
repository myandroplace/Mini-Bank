package com.minibank.exception;

/**
 * Выбрасывается, когда запрашиваемый банковский счет не существует в системе.
 */
public class AccountNotFoundException extends BankException {

    private final Long accountId;

    public AccountNotFoundException(Long accountId) {
        super(String.format("Банковский счет с идентификатором %d не найден", accountId));
        this.accountId = accountId;
    }

    public AccountNotFoundException(String message) {
        super(message);
        this.accountId = null;
    }

    public Long getAccountId() {
        return accountId;
    }
}
