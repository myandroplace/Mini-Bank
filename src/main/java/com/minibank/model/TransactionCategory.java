package com.minibank.model;

/**
 * Категории расходов и доходов для аналитики и учета.
 */
public enum TransactionCategory {
    SALARY("Зарплата"),
    GROCERIES("Продукты и супермаркеты"),
    UTILITIES("Коммунальные платежи"),
    ENTERTAINMENT("Развлечения и отдых"),
    INVESTMENTS("Инвестиции"),
    TRANSFER("Перевод между счетами"),
    OTHER("Прочее");

    private final String displayName;

    TransactionCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
