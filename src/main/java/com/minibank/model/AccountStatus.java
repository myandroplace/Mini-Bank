package com.minibank.model;

/**
 * Перечисление статусов банковского счета.
 * Enum обеспечивает типобезопасность: счет не может находиться
 * в неопределенном или некорректном строковом состоянии.
 */
public enum AccountStatus {
    ACTIVE("Активен"),
    BLOCKED("Заблокирован"),
    CLOSED("Закрыт");

    private final String description;

    AccountStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Проверяет, можно ли выполнять финансовые операции по счету.
     */
    public boolean isOperational() {
        return this == ACTIVE;
    }
}
