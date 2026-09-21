package com.minibank.exception;

import com.minibank.model.AccountStatus;

/**
 * Выбрасывается при попытке провести операцию по заблокированному или закрытому счету.
 */
public class AccountBlockedException extends BankException {

    private final Long accountId;
    private final AccountStatus status;

    public AccountBlockedException(Long accountId, AccountStatus status) {
        super(String.format("Операция отклонена: счет #%d находится в неактивном статусе '%s'",
                accountId, status.getDescription()));
        this.accountId = accountId;
        this.status = status;
    }

    public Long getAccountId() {
        return accountId;
    }

    public AccountStatus getStatus() {
        return status;
    }
}
