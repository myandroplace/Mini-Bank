package com.minibank.model;

/**
 * Тип финансовой операции:
 * - DEPOSIT: прямое пополнение счета
 * - WITHDRAWAL: снятие средств со счета
 * - TRANSFER: перевод между счетами
 */
public enum TransactionType {
    DEPOSIT("Пополнение"),
    WITHDRAWAL("Снятие"),
    TRANSFER("Перевод");

    private final String title;

    TransactionType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
